package rut.miit.tech.summer_hackathon.service.department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.repository.DepartmentRepository;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    //TODO: реализовать критерии api
    private final DepartmentRepository departmentRepository;

    @Override
    public Department getById(Long id) {
        return null;
    }

    @Override
    public Department getByName(String name) {
        return null;
    }

    @Override
    public List<Department> getAll() {
        return List.of();
    }

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public Department update(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public PageResult<List<Department>> findAll(int page, int pageSize) {
        return null;
    }
}
