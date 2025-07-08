package rut.miit.tech.summer_hackathon.service.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

public interface UserService {
    User save(User user);
    User getById(Long id);
    PageResult<User> getAll(Specification<User> filter, Pageable pageable);
    User update(Long id, User user);
    void delete(Long id);
    PageResult<User> getUserByDepartmentId(Long depId, Specification<User> filter,
                                           Pageable pageable);

    PageResult<User> getAllByRequest(String request, Pageable pageable);

}