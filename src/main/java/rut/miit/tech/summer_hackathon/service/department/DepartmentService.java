package rut.miit.tech.summer_hackathon.service.department;

import org.springframework.data.domain.Pageable; // Интерфейс пагинации
import org.springframework.data.jpa.domain.Specification; // Фильтрация через спецификации
import rut.miit.tech.summer_hackathon.domain.model.Department; // Сущность департамента
import rut.miit.tech.summer_hackathon.service.util.PageResult; // Кастомный класс пагинации

import java.util.List;
import java.util.Optional;


public interface DepartmentService {

    //done
    Department getById(Long id);


     Optional<Department> getByName(String name);

    // я так понимаю не надо его реализовывать
    List<Department> saveAll(List<Department> departments);

    // done
    PageResult<Department> getAll(Specification<Department> filter, Pageable pageable);

    // я так понимаю не надо его реализовывать
    List<Department> getAllByIds(List<Long> ids);

    //done
    Department save(Department department);


    Department update(Department department);


    void deleteById(Long id);


    PageResult<List<Department>> findAll(Pageable pageable);


    List<Department> getAllByModeratorId(Long id);

    List<Department> getAllDepartmentsByUserId(Long id);


    List<Department> updateAll(List<Department> newDeps);


    PageResult<Department> getAllByRequest(String request, Pageable pageable);

    // Добавить пользователя в департамент
    void addUserToDepartment(Long departmentId, Long userId);

    // Удалить пользователя из департамента
    void removeUserFromDepartment(Long departmentId, Long userId);
}