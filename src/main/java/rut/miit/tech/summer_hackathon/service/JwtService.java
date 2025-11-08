package rut.miit.tech.summer_hackathon.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;
import rut.miit.tech.summer_hackathon.repository.RefreshTokenRepository;
import rut.miit.tech.summer_hackathon.repository.RevokedTokenRepository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${security.jwt.access.secret}")
    private String accessSecret;
    @Value("${security.jwt.access.expires}")
    private Duration accessLifetime;

    @Value("${security.jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${security.jwt.refresh.expires}")
    private Duration refreshLifetime;

    @org.springframework.beans.factory.annotation.Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public DecodedJWT decodeAccessToken(String token) {
        return JWT.require(Algorithm.HMAC256(accessSecret))
                .withClaim("purpose", "access")
                .withIssuer("security-api")
                .build().verify(token);
    }

    public String generateAccessToken(@NotNull UserDetails user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuer("security-api")
                .withClaim("purpose", "access")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(accessLifetime)))
                .withArrayClaim("roles", user.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .toList()
                        .toArray(new String[0]))
                .sign(Algorithm.HMAC256(accessSecret));
    }

    public String generateRefreshToken(@NotNull UserDetails user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuer("security-api")
                .withClaim("purpose", "refresh")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(refreshLifetime)))
                .withArrayClaim("roles", user.getAuthorities().stream().map(Object::toString).toList().toArray(new String[0]))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(refreshSecret));
    }

    public DecodedJWT decodeRefreshToken(String token) {
        return JWT.require(Algorithm.HMAC256(refreshSecret))
                .withClaim("purpose", "refresh")
                .withIssuer("security-api")
                .build().verify(token);
    }

    public String createAccess(@NotNull DecodedJWT refreshToken) {
        String jti = refreshToken.getId();

        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (stored.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return JWT.create()
                .withSubject(refreshToken.getSubject())
                .withIssuer("security-api")
                .withClaim("purpose", "access")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(accessLifetime)))
                .withArrayClaim("roles", refreshToken.getClaim("roles").asArray(String.class))
                .sign(Algorithm.HMAC256(accessSecret));
    }
}