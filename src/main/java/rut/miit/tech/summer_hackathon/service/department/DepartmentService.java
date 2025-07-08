package rut.miit.tech.summer_hackathon.service.department;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

public interface DepartmentService {
    Department getById(Long id);
    Department getByName(String name);
    List<Department> saveAll(List<Department> departments);
    PageResult<Department> getAll(Specification<Department> filter, Pageable pageable);
    List<Department> getAllByIds(List<Long> ids);
    Department save(Department department);
    Department update(Department department);
    void deleteById(Long id);
    PageResult<List<Department>> findAll(int page, int pageSize);

    List<Department> getAllByModeratorId(Long id);

    List<Department> updateAll(List<Department> newDeps);

    List<Department> getAllByRequest(String request);
    //TODO: реализовать в пакете util класс FilterUnit и SortUnit универсальный для всех сервисов

}
