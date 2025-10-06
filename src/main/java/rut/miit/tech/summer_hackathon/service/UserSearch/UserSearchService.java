package rut.miit.tech.summer_hackathon.service.UserSearch;


import org.springframework.data.domain.Pageable;
import rut.miit.tech.summer_hackathon.service.util.PageResult;
import rut.miit.tech.summer_hackathon.domain.model.User;

public interface UserSearchService {
    PageResult<User> searchAllUsers(String request, Pageable pageable);
}