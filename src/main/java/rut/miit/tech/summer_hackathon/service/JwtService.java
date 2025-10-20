package rut.miit.tech.summer_hackathon.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import rut.miit.tech.summer_hackathon.repository.RevokedTokenRepository;
import rut.miit.tech.summer_hackathon.repository.RefreshTokenRepository;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;

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

    private final RevokedTokenRepository revokedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public DecodedJWT decodeAccessToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(accessSecret))
                    .withClaim("purpose","access")
                    .withIssuer("security-api")
                    .build().verify(token);

            // Проверка, не отозван ли access токен
            if (isAccessTokenRevoked(decodedJWT.getId())) {
                throw new RuntimeException("Access token revoked");
            }

            // Проверка, не отозван ли refresh-токен, на котором был создан этот access-токен
            String refreshJti = decodedJWT.getClaim("refresh_jti").asString();
            if (refreshJti != null) {
                RefreshToken rt = refreshTokenRepository.findByJti(refreshJti)
                        .orElseThrow(() -> new RuntimeException("Associated refresh token not found"));
                if (rt.isRevoked()) {
                    throw new RuntimeException("Access token based on revoked refresh token");
                }
            }

            return decodedJWT;
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new RuntimeException("Access token expired");
        }
    }

    // Добавлен параметр refreshToken
    public String generateAccessToken(@NotNull UserDetails user, String refreshToken) {
        DecodedJWT decodedRefresh = JWT.decode(refreshToken);
        String refreshJti = decodedRefresh.getId();

        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuer("security-api")
                .withClaim("purpose","access")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(accessLifetime)))
                .withArrayClaim("roles", user.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .toList()
                        .toArray(new String[0]))
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("refresh_jti", refreshJti) // <-- добавляем refresh_jti
                .sign(Algorithm.HMAC256(accessSecret));
    }

    //генерация самого рефреша
    public String generateRefreshToken(@NotNull UserDetails user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuer("security-api")
                .withClaim("purpose","refresh")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(refreshLifetime)))
                .withArrayClaim("roles", user.getAuthorities().stream().map(Object::toString).toList().toArray(new String[0]))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(refreshSecret));
    }

    //Декодирование рефреша, чтобы проверить его
    public DecodedJWT decodeRefreshToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(refreshSecret))
                    .withClaim("purpose", "refresh")
                    .withIssuer("security-api")
                    .build().verify(token);

            // Дополнительная проверка в базе данных
            String jti = decodedJWT.getId();
            RefreshToken stored = refreshTokenRepository.findByJti(jti)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            if (stored.isRevoked()) {
                throw new RuntimeException("Refresh token revoked");
            }

            if (stored.getExpiresAt().isBefore(Instant.now())) {
                throw new RuntimeException("Refresh token expired");
            }

            return decodedJWT;
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new RuntimeException("Refresh token expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    //Это создание нового аксесса, если рефреш существует и он не ревокнут
    public String createAccess(@NotNull DecodedJWT refreshToken) {
        String jti = refreshToken.getId();
        //Проверка на то, что он существует
        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        //Проверка, чтобы токен существовал, но не был реворкнут
        if (stored.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }
        //Проверка, что время токена не вышло
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        //Все проверки прошли? Значит генерим новый аксесс токен
        return JWT.create()
                .withSubject(refreshToken.getSubject())
                .withIssuer("security-api")
                .withClaim("purpose", "access")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(accessLifetime)))
                .withArrayClaim("roles", refreshToken.getClaim("roles").asArray(String.class))
                .withClaim("refresh_jti", jti) // <-- тоже добавляем refresh_jti
                .sign(Algorithm.HMAC256(accessSecret));
    }

    public boolean isAccessTokenRevoked(String jti) {
        if (jti == null) return false;
        return revokedTokenRepository.findByToken(jti).isPresent();
    }
}