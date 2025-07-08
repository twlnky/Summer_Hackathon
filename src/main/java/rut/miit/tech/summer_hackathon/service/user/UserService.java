package rut.miit.tech.summer_hackathon.service.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

public interface UserService {
    User save(User user);
    User getById(Long id);
    PageResult<User> getAll(Specification<User> filter, Pageable pageable);
    User update(User user);
    void delete(Long id);

    List<User> getAllByRequest(String request);

}