package rut.miit.tech.summer_hackathon.service.department;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;
import java.util.Optional;


public interface DepartmentService {


    Department getById(Long id);


    Optional<Department> getByName(String name);


    List<Department> saveAll(List<Department> departments);


    PageResult<Department> getAll(Specification<Department> filter, Pageable pageable);


    List<Department> getAllByIds(List<Long> ids);


    Department save(Department department);


    Department update(Long id, Department department);


    void deleteById(Long id);


    PageResult<List<Department>> findAll(Pageable pageable);


    List<Department> getAllByModeratorId(Long id);

    List<Department> getAllDepartmentsByUserId(Long id);


    List<Department> updateAll(List<Department> newDeps);


    PageResult<Department> getAllByRequest(String request, Pageable pageable);


    void addUserToDepartment(Long departmentId, Long userId);


    void removeUserFromDepartment(Long departmentId, Long userId);
}