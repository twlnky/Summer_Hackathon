package rut.miit.tech.summer_hackathon.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.controller.query.PageParam;
import rut.miit.tech.summer_hackathon.controller.query.SortParam;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.user.UserService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    public UserDTO update(@PathVariable Long id,
                          @RequestBody UserDTO userDTO) {

        return userService.update(id, userDTO.toModel()).toDto();
    }


    @GetMapping("/public")
    public PageResult<UserDTO> getAllUsers(@ModelAttribute UserFilter userFilter,
                                           @ModelAttribute PageParam pageParam,
                                           @ModelAttribute SortParam sortParam) {
        return userService.getAll(userFilter, pageParam.toPageable(sortParam)).map(User::toDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    public PageResult<UserDTO> getAllUsersPrivate(@ModelAttribute UserFilter userFilter,
                                                  @ModelAttribute PageParam pageParam,
                                                  @ModelAttribute SortParam sortParam) {
        return userService.getAll(userFilter, pageParam.toPageable(sortParam)).map(User::toDto);
    }


    @GetMapping("/public/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getById(id).toDto();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserDTO userDTO) {
        return userService.save(userDTO.toModel()).toDto();
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    public void delete(@PathVariable Long id) {

        userService.delete(id);
    }


}