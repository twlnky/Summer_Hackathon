package rut.miit.tech.summer_hackathon.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.UserSearch.UserSearchService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping("/search")
    public PageResult<User> searchUsers(@RequestParam String query,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userSearchService.searchAllUsers(query, pageable);
    }
}
