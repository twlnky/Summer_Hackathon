package rut.miit.tech.summer_hackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository; // Базовый интерфейс CRUD-операций
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Для спецификаций/динамических запросов
import org.springframework.stereotype.Repository; // Помечает интерфейс как репозиторий
import rut.miit.tech.summer_hackathon.domain.model.Department; // Сущность департамента
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long>,
        JpaSpecificationExecutor<Department> {


    Optional<Department> findByName(String name);

    List<Department> findAllByUsersId(Long userId);
}