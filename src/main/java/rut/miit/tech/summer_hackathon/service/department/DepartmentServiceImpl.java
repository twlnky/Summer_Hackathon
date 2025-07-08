package rut.miit.tech.summer_hackathon.service.department;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.tech.summer_hackathon.controller.DepartmentFilter;
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
    public List<Department> saveAll(List<Department> departments) {
        return List.of();
    }

    @Override
    public PageResult<Department> getAll(Specification<Department> filter, Pageable pageable) {
        return PageResult.of(departmentRepository.findAll(filter, pageable),pageable);
    }

    @Override
    public List<Department> getAllByIds(List<Long> ids) {
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

    @Override
    public List<Department> getAllByModeratorId(Long id) {
        return List.of();
    }

    @Override
    public List<Department> updateAll(List<Department> newDeps) {
        return List.of();
    }

    @Override
    public List<Department> getAllByRequest(String request) {
        Pageable pageable = PageRequest.of(0, 10);
        if(request.isBlank()){
            return getAll(new DepartmentFilter(), pageable).getQueryResult();
        }
        return departmentRepository.findAll(
                (root, query, cb) -> cb.or(
                        cb.like(root.get("name"), "%" + request + "%"),
                        cb.isMember(request.toLowerCase(), root.get("tags"))
                ), pageable
        ).toList();
    }
}
