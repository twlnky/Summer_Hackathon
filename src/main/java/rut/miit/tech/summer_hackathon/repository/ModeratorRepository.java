package rut.miit.tech.summer_hackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

import java.util.Optional;


public interface ModeratorRepository
        extends JpaRepository<Moderator, Long>,
        JpaSpecificationExecutor<Moderator> {

    Optional<Moderator> findByLogin(String login);
}