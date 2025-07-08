package rut.miit.tech.summer_hackathon.service.department;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.tech.summer_hackathon.controller.department.DepartmentFilter;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.repository.DepartmentRepository;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;


    @Override
    public PageResult<Department> getAll(Specification<Department> filter, Pageable pageable) {
        filter = filter.and(
                ((root, query, cb) -> {
                    root.fetch("moderator", JoinType.LEFT);
                    return cb.and();
                })
        );
        return PageResult.of(
                departmentRepository.findAll(filter, pageable), // Стандартный запрос JPA
                pageable // Сохраняет параметры пагинации
        );
    }


    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }


    @Override
    public Department update(Department department) {
        return departmentRepository.save(department); // JPA автоматически определяет INSERT/UPDATE
    }


    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id); // Прямое делегирование
    }


    @Override
    public PageResult<Department> getAllByRequest(String request, Pageable pageable) {
        if (request.isBlank()) {
            // Использует фильтр по умолчанию при пустом запросе
            return getAll(new DepartmentFilter(), pageable);
        }
        Specification<Department> filter = (root, query, cb) -> cb.or(
                // Поиск по имени: нечувствительный к регистру LIKE
                cb.like(cb.lower(root.get("name")), "%" + request.toLowerCase() + "%"),

                // Поиск в тегах: проверяет наличие строки в массиве tags
                cb.isMember(request.toLowerCase(), root.get("tags"))
        );
        return PageResult.of(departmentRepository.findAll(filter,
                pageable
        ), pageable);
    }

    @Override
    public List<Department> getAllDepartmentsByUserId(Long id) {
        return departmentRepository.findAll(
                ((root, query, cb) ->
                        cb.equal(root.get("users").get("id"),id))
        );
    }

    //________________________________________________________________Доделать если надо будет
    @Override
    public Department getById(Long id) {
        return null;
    }

    @Override
    public Optional<Department> getByName(String name) {
        return departmentRepository.findByName(name);
    }

    @Override
    public List<Department> saveAll(List<Department> departments) {
        return List.of();
    }

    @Override
    public List<Department> getAllByIds(List<Long> ids) {
        return List.of();
    }

    @Override
    public PageResult<List<Department>> findAll(Pageable pageable) {
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
}