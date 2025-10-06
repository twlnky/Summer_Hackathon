package rut.miit.tech.summer_hackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;

import java.util.Optional;

//Интерфейс, чтобы находить по айдишнику токен
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);
}
