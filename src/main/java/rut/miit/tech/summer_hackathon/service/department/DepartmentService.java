package rut.miit.tech.summer_hackathon.service.department;

import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

public interface DepartmentService {
    Department getById(Long id);
    Department getByName(String name);
    List<Department> getAll();
    Department save(Department department);
    Department update(Department department);
    void deleteById(Long id);
    PageResult<List<Department>> findAll(int page, int pageSize);
    //TODO: реализовать в пакете util класс FilterUnit и SortUnit универсальный для всех сервисов

}
