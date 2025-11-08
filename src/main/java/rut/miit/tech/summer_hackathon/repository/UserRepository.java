package rut.miit.tech.summer_hackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rut.miit.tech.summer_hackathon.domain.model.User;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {


    boolean existsUserByEmail(String email);
}