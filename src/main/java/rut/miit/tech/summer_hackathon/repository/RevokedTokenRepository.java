package rut.miit.tech.summer_hackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.miit.tech.summer_hackathon.domain.model.RevokedToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    List<RevokedToken> findAllByRevokedAtBefore(Instant instant);
}
