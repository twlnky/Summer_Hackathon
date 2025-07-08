package rut.miit.tech.summer_hackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.miit.tech.summer_hackathon.domain.model.ConfirmToken;


public interface ConfirmTokenRepository
        extends JpaRepository<ConfirmToken, Long> {

    ConfirmToken findByToken(int token);
}