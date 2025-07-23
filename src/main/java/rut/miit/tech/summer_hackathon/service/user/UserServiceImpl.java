package rut.miit.tech.summer_hackathon.service.user;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.controller.user.UserFilter;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.security.SecurityUtils;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    @Override
    public PageResult<User> getAll(Specification<User> filter, Pageable pageable) {
        Page<User> page = userRepository.findAll(filter, pageable);
        return PageResult.of(page, pageable);
    }

    @Override
    public User update(Long id, User user) {
        user.setId(id);
        User beforeUpdateUser = getById(id);
        if (SecurityUtils.isAdmin()) {
            return userRepository.save(user);
        }
        if (beforeUpdateUser.getModerator() == null) {
            throw new AccessDeniedException("Cannot update user, because you're not legal moderator");
        }
        Moderator authenticatedModerator = securityUtils.getAuthenticatedModerator();
        //Если текущий модератор - тот же модератор что и в базе данных
        //То есть имеет полные права на обновление
        if (authenticatedModerator.getId().equals(beforeUpdateUser.getModerator().getId())) {
            //TODO: проверка на правильность обновление подразделений
            return userRepository.save(user);
        }
        //Если редактирует не основной модератор - нужно проверить, корректность обновления
        //Если есть попытка обновление основных атрибутов - отказываем
        if (!user.equals(beforeUpdateUser)) {
            throw new AccessDeniedException("Cannot update user attributes, because you're not main moderator");
        }
        //Если есть попытка переназначить модератора
        if (!user.getModerator().getId().equals(beforeUpdateUser.getModerator().getId())) {
            throw new AccessDeniedException("Cannot update user's moderator, only admin has that permission");
        }
        //Проверка на то, что изменены, только подразделения, управляемые модераторомм
        //Если модератор задел связи с другими подразделениями - это нарушение прав
        if (!checkModeratorRelationsViolation(beforeUpdateUser,
                user,
                authenticatedModerator)) {
            throw new AccessDeniedException("Cannot update user's relations to other departments, not controled by current moderator");
        }
        return userRepository.save(user);
    }

    private boolean checkModeratorRelationsViolation(User beforeUpdateUser,
                                                     User currentUser,
                                                     Moderator currentModer) {
        List<Department> beforeUpdateDepartments = new ArrayList<>(departmentService.getAllDepartmentsByUserId(beforeUpdateUser.getId())
                .stream().toList());
        beforeUpdateDepartments.removeIf(
                d -> d.getModerator().getId().equals(currentModer.getId())
        );
        Set<Long> currentDepartments = currentUser.getDepartments()
                .stream().map(Department::getId).collect(Collectors.toSet());
        return currentDepartments.containsAll(beforeUpdateDepartments
                .stream().map(Department::getId).toList());
    }

    @Override
    public void delete(Long id) {
        User user = getById(id);
        if(SecurityUtils.isAdmin()){
            userRepository.deleteById(id);
            return;
        }
        Moderator moderator = securityUtils.getAuthenticatedModerator();
        if (user.getModerator() == null) {
            throw new AccessDeniedException("Cannot delete user, because you're not legal moderator");
        }
        if(!user.getModerator().getId().equals(moderator.getId())){
            throw new AccessDeniedException("Cannot delete user, because you're not main moderator");
        }
        userRepository.deleteById(id);
    }

    @Override
    public PageResult<User> getUserByDepartmentId(Long depId, Specification<User> filter, Pageable pageable) {
        filter = filter.and(
                ((root, query, cb) -> cb.equal(root.get("departments").get("id"), depId))
        );
        return PageResult.of(userRepository.findAll(filter, pageable), pageable);
    }

    @Override
    public PageResult<User> getAllByRequest(String request, Pageable pageable) {
        if (request.isBlank()) {
            return getAll(new UserFilter(), pageable);
        }
        
        Specification<User> filter = ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Поиск по текстовым полям - добавим вариант без LOWER для кириллицы
            String lowerRequest = request.toLowerCase();
            
            // Поиск по именам и фамилиям - как с LOWER, так и без
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + lowerRequest + "%"));
            predicates.add(cb.like(root.get("firstName"), "%" + request + "%"));
            predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lowerRequest + "%"));
            predicates.add(cb.like(root.get("lastName"), "%" + request + "%"));
            predicates.add(cb.like(cb.lower(root.get("middleName")), "%" + lowerRequest + "%"));
            predicates.add(cb.like(root.get("middleName"), "%" + request + "%"));
            
            // Email и другие поля
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + lowerRequest + "%"));
            predicates.add(cb.like(root.get("personalPhone"), "%" + request + "%"));
            predicates.add(cb.like(cb.lower(root.get("position")), "%" + lowerRequest + "%"));
            predicates.add(cb.like(root.get("position"), "%" + request + "%"));
            predicates.add(cb.like(cb.lower(root.get("note")), "%" + lowerRequest + "%"));
            predicates.add(cb.like(root.get("note"), "%" + request + "%"));
            
            // Поиск по числовому полю officeNumber
            try {
                Long officeNumberLong = Long.parseLong(request);
                predicates.add(cb.equal(root.get("officeNumber"), officeNumberLong));
            } catch (NumberFormatException e) {
                // Если запрос не является числом, просто игнорируем поиск по officeNumber
            }
            
            return cb.or(predicates.toArray(new Predicate[0]));
        });
        
        return PageResult.of(userRepository.findAll(filter, pageable), pageable);
    }

}