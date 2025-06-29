package rut.miit.tech.summer_hackathon.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.model.User;
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

    @Value("${security.jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${security.jwt.access.expires}")
    private Duration accessLifetime;

    @Value("${security.jwt.refresh.expires}")
    private Duration refreshLifetime;

    private final RevokedTokenRepository revokedTokenRepository;

    public DecodedJWT decodeAccessToken(String token) {
        return JWT.require(Algorithm.HMAC256(accessSecret))
                .withClaim("purpose","access")
                .withIssuer("security-api")
                .build().verify(token);
    }

    public String generateAccessToken(@NotNull User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuer("security-api")
                .withClaim("purpose","access")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(accessLifetime)))
                .withArrayClaim("roles",user.getRoles()
                        .stream()
                        .map(Object::toString)
                        .toList()
                        .toArray(new String[0]))
                .sign(Algorithm.HMAC256(accessSecret));
    }

    public String generateRefreshToken(@NotNull User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuer("security-api")
                .withClaim("purpose","refresh")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(refreshLifetime)))
                .withArrayClaim("roles",user.getRoles().stream().map(Object::toString).toList().toArray(new String[0]))
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
        String tokenId = refreshToken.getId();
        if (revokedTokenRepository.findByToken(tokenId).isPresent()) {
            throw new RuntimeException("Токен использован");
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
