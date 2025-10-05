package rut.miit.tech.summer_hackathon.service.department;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.tech.summer_hackathon.controller.department.DepartmentFilter;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.DepartmentRepository;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;


    @Override
    public PageResult<Department> getAll(Specification<Department> filter, Pageable pageable) {

        return PageResult.of(
                departmentRepository.findAll(filter, pageable),
                pageable
        );
    }


    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }


    @Override
    public Department update(Long id, Department department) {

        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));


        existingDepartment.setName(department.getName());
        if (department.getModerator() != null) {
            existingDepartment.setModerator(department.getModerator());
        }

        return departmentRepository.save(existingDepartment);
    }


    @Override
    public void deleteById(Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));


        Specification<User> spec = (root, query, criteriaBuilder) -> {
            return criteriaBuilder.isMember(department, root.get("departments"));
        };


        List<User> usersInDepartment = userRepository.findAll(spec);


        for (User user : usersInDepartment) {
            user.getDepartments().remove(department);
            userRepository.save(user);
        }


        departmentRepository.deleteById(id);
    }


    @Override
    public PageResult<Department> getAllByRequest(String request, Pageable pageable) {
        if (request.isBlank()) {

            return getAll(new DepartmentFilter(), pageable);
        }
        Specification<Department> filter = (root, query, cb) -> cb.or(

                cb.like(cb.lower(root.get("name")), "%" + request.toLowerCase() + "%"),


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
                        cb.equal(root.get("users").get("id"), id))
        );
    }

    @Override
    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
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

    @Override
    public void addUserToDepartment(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (!user.getDepartments().contains(department)) {
            user.getDepartments().add(department);
            userRepository.save(user);
        }
    }

    @Override
    public void removeUserFromDepartment(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        user.getDepartments().remove(department);
        userRepository.save(user);
    }
}