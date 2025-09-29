package rut.miit.tech.summer_hackathon.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${security.jwt.access.secret}")
    private String accessSecret;
    @Value("${security.jwt.access.expires}")
    private Duration accessLifetime;

    public DecodedJWT decodeAccessToken(String token) {
        return JWT.require(Algorithm.HMAC256(accessSecret))
                .withClaim("purpose","access")
                .withIssuer("security-api")
                .build().verify(token);
    }

    public String generateAccessToken(@NotNull UserDetails user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuer("security-api")
                .withClaim("purpose","access")
                .withIssuedAt(Timestamp.from(Instant.now()))
                .withExpiresAt(Timestamp.from(Instant.now().plus(accessLifetime)))
                .withArrayClaim("roles",user.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .toList()
                        .toArray(new String[0]))
                .sign(Algorithm.HMAC256(accessSecret));
    }

}
