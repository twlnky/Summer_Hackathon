package rut.miit.tech.summer_hackathon.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;
import rut.miit.tech.summer_hackathon.repository.RefreshTokenRepository;
import rut.miit.tech.summer_hackathon.repository.RevokedTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtService jwtService;
    private UserDetails userDetails;
    private final String accessSecret = "testAccessSecretKeyWithMinimumLength32";
    private final String refreshSecret = "testRefreshSecretKeyWithMinimumLength32";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "accessSecret", accessSecret);
        ReflectionTestUtils.setField(jwtService, "accessLifetime", Duration.ofHours(1));
        ReflectionTestUtils.setField(jwtService, "refreshSecret", refreshSecret);
        ReflectionTestUtils.setField(jwtService, "refreshLifetime", Duration.ofDays(7));

        ReflectionTestUtils.setField(jwtService, "revokedTokenRepository", revokedTokenRepository);
        ReflectionTestUtils.setField(jwtService, "refreshTokenRepository", refreshTokenRepository);

        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
    }

    @Test
    void generateAccessToken_ValidUser_ReturnsValidToken() {

        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        DecodedJWT decoded = jwtService.decodeAccessToken(token);
        assertEquals("testuser", decoded.getSubject());
        assertEquals("security-api", decoded.getIssuer());
        assertEquals("access", decoded.getClaim("purpose").asString());
        assertTrue(decoded.getExpiresAt().toInstant().isAfter(Instant.now()));

        String[] roles = decoded.getClaim("roles").asArray(String.class);
        assertEquals(1, roles.length);
        assertEquals("ROLE_USER", roles[0]);
    }

    @Test
    void generateRefreshToken_ValidUser_ReturnsValidToken() {

        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        DecodedJWT decoded = jwtService.decodeRefreshToken(token);
        assertEquals("testuser", decoded.getSubject());
        assertEquals("security-api", decoded.getIssuer());
        assertEquals("refresh", decoded.getClaim("purpose").asString());
        assertNotNull(decoded.getId());
        assertTrue(decoded.getExpiresAt().toInstant().isAfter(Instant.now()));

        String[] roles = decoded.getClaim("roles").asArray(String.class);
        assertEquals(1, roles.length);
        assertEquals("ROLE_USER", roles[0]);
    }

    @Test
    void decodeAccessToken_InvalidToken_ThrowsException() {

        String invalidToken = "invalid.token.here";

        assertThrows(JWTVerificationException.class,
                () -> jwtService.decodeAccessToken(invalidToken));
    }

    @Test
    void decodeAccessToken_TokenWithWrongPurpose_ThrowsException() {

        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertThrows(JWTVerificationException.class,
                () -> jwtService.decodeAccessToken(refreshToken));
    }

    @Test
    void decodeRefreshToken_InvalidToken_ThrowsException() {

        String invalidToken = "invalid.token.here";

        assertThrows(JWTVerificationException.class,
                () -> jwtService.decodeRefreshToken(invalidToken));
    }

    @Test
    void decodeRefreshToken_TokenWithWrongPurpose_ThrowsException() {

        String accessToken = jwtService.generateAccessToken(userDetails);

        assertThrows(JWTVerificationException.class,
                () -> jwtService.decodeRefreshToken(accessToken));
    }

    @Test
    void createAccess_ValidRefreshToken_ReturnsNewAccessToken() {

        String refreshToken = jwtService.generateRefreshToken(userDetails);
        DecodedJWT decodedRefresh = jwtService.decodeRefreshToken(refreshToken);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setJti(decodedRefresh.getId());
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(decodedRefresh.getExpiresAt().toInstant());

        when(refreshTokenRepository.findByJti(decodedRefresh.getId()))
                .thenReturn(Optional.of(storedToken));

        String newAccessToken = jwtService.createAccess(decodedRefresh);

        assertNotNull(newAccessToken);
        DecodedJWT decodedAccess = jwtService.decodeAccessToken(newAccessToken);
        assertEquals("testuser", decodedAccess.getSubject());
        assertEquals("access", decodedAccess.getClaim("purpose").asString());

        String[] roles = decodedAccess.getClaim("roles").asArray(String.class);
        assertEquals(1, roles.length);
        assertEquals("ROLE_USER", roles[0]);
    }

    @Test
    void createAccess_RevokedToken_ThrowsException() {

        String refreshToken = jwtService.generateRefreshToken(userDetails);
        DecodedJWT decodedRefresh = jwtService.decodeRefreshToken(refreshToken);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setJti(decodedRefresh.getId());
        storedToken.setRevoked(true);
        storedToken.setExpiresAt(decodedRefresh.getExpiresAt().toInstant());

        when(refreshTokenRepository.findByJti(decodedRefresh.getId()))
                .thenReturn(Optional.of(storedToken));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtService.createAccess(decodedRefresh));
        assertEquals("Refresh token revoked", exception.getMessage());
    }

    @Test
    void createAccess_ExpiredToken_ThrowsException() {

        String refreshToken = jwtService.generateRefreshToken(userDetails);
        DecodedJWT decodedRefresh = jwtService.decodeRefreshToken(refreshToken);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setJti(decodedRefresh.getId());
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(Instant.now().minus(Duration.ofDays(1))); // Прошедшая дата

        when(refreshTokenRepository.findByJti(decodedRefresh.getId()))
                .thenReturn(Optional.of(storedToken));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtService.createAccess(decodedRefresh));
        assertEquals("Refresh token expired", exception.getMessage());
    }

    @Test
    void createAccess_NotFoundToken_ThrowsException() {

        String refreshToken = jwtService.generateRefreshToken(userDetails);
        DecodedJWT decodedRefresh = jwtService.decodeRefreshToken(refreshToken);

        when(refreshTokenRepository.findByJti(anyString()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtService.createAccess(decodedRefresh));
        assertEquals("Refresh token not found", exception.getMessage());
    }

    @Test
    void tokens_WithDifferentSecrets_AreNotInterchangeable() {

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);


        assertThrows(JWTVerificationException.class,
                () -> jwtService.decodeRefreshToken(accessToken));

        assertThrows(JWTVerificationException.class,
                () -> jwtService.decodeAccessToken(refreshToken));
    }

    @Test
    void generateTokens_WithMultipleRoles_ReturnsValidTokens() {

        UserDetails userWithMultipleRoles = User.withUsername("admin")
                .password("password")
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
                .build();

        String accessToken = jwtService.generateAccessToken(userWithMultipleRoles);
        String refreshToken = jwtService.generateRefreshToken(userWithMultipleRoles);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        DecodedJWT decodedAccess = jwtService.decodeAccessToken(accessToken);
        DecodedJWT decodedRefresh = jwtService.decodeRefreshToken(refreshToken);

        String[] accessRoles = decodedAccess.getClaim("roles").asArray(String.class);
        String[] refreshRoles = decodedRefresh.getClaim("roles").asArray(String.class);

        assertEquals(2, accessRoles.length);
        assertEquals(2, refreshRoles.length);
        assertTrue(Arrays.asList(accessRoles).contains("ROLE_USER"));
        assertTrue(Arrays.asList(accessRoles).contains("ROLE_ADMIN"));
        assertTrue(Arrays.asList(refreshRoles).contains("ROLE_USER"));
        assertTrue(Arrays.asList(refreshRoles).contains("ROLE_ADMIN"));
    }

    @Test
    void generateTokens_WithEmptyAuthorities_ReturnsValidTokens() {

        UserDetails userWithNoRoles = User.withUsername("noprivileges")
                .password("password") // нет назначаем authorities
                .build();

        String accessToken = jwtService.generateAccessToken(userWithNoRoles);
        String refreshToken = jwtService.generateRefreshToken(userWithNoRoles);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        DecodedJWT decodedAccess = jwtService.decodeAccessToken(accessToken);
        DecodedJWT decodedRefresh = jwtService.decodeRefreshToken(refreshToken);

        String[] accessRoles = decodedAccess.getClaim("roles").asArray(String.class);
        String[] refreshRoles = decodedRefresh.getClaim("roles").asArray(String.class);

        assertNotNull(accessRoles);
        assertNotNull(refreshRoles);
        assertEquals(0, accessRoles.length);
        assertEquals(0, refreshRoles.length);
    }
}