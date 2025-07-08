package rut.miit.tech.summer_hackathon.service.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.controller.user.UserFilter;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

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
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllByRequest(String request) {
        Pageable pageable = PageRequest.of(0,10);
        if(request.isBlank()){
            return getAll(new UserFilter(), pageable).getQueryResult();
        }
        return userRepository.findAll(((root, query, cb) ->
                cb.or(
                    cb.like(root.get("firstName"),"%" + request + "%"),
                    cb.like(root.get("lastName"),"%" + request + "%"),
                    cb.like(root.get("middleName"),"%" + request + "%")
                )),pageable).toList();
    }

}