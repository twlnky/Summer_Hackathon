package rut.miit.tech.summer_hackathon.controller.department;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.controller.query.PageParam;
import rut.miit.tech.summer_hackathon.controller.query.SortParam;
import rut.miit.tech.summer_hackathon.controller.user.UserFilter;
import rut.miit.tech.summer_hackathon.domain.dto.DepartmentDTO;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.user.UserService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private  final DepartmentService departmentService;
    private final UserService userService;

    @GetMapping("/public")
    public PageResult<DepartmentDTO> getAll(@ModelAttribute DepartmentFilter departmentFilter,
                                            @ModelAttribute PageParam pageParam,
                                            @ModelAttribute SortParam sortParam){
        return departmentService.getAll(departmentFilter, pageParam.toPageable(sortParam)).map(Department::toDto);
    }

    @GetMapping("public/{id}/users")
    public PageResult<UserDTO> getUsersByDepartment(@ModelAttribute UserFilter userFilter,
                                                    @ModelAttribute PageParam pageParam,
                                                    @ModelAttribute SortParam sortParam,
                                                    @PathVariable Long id){

        return userService.getUserByDepartmentId(id, userFilter, pageParam.toPageable(sortParam)).map(User::toDto);

    }

    @GetMapping("/public/{id}")
    public DepartmentDTO getDepartmentById(@ModelAttribute DepartmentFilter departmentFilter,
                                           @ModelAttribute PageParam pageParam,
                                           @ModelAttribute SortParam sortParam,
                                           @PathVariable Long id){
        return departmentService.getById(id).toDto();
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public DepartmentDTO create(@Valid @RequestBody DepartmentDTO departmentDTO){
        return departmentService.save(departmentDTO.toModel()).toDto();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public DepartmentDTO update(@PathVariable Long id, @Valid @RequestBody DepartmentDTO departmentDTO){
        return departmentService.update(id, departmentDTO.toModel()).toDto();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        departmentService.deleteById(id);
    }


    @PostMapping("/{departmentId}/users/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    @ResponseStatus(code = HttpStatus.OK)
    public void addUserToDepartment(@PathVariable Long departmentId, @PathVariable Long userId) {
        departmentService.addUserToDepartment(departmentId, userId);
    }


    @DeleteMapping("/{departmentId}/users/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeUserFromDepartment(@PathVariable Long departmentId, @PathVariable Long userId) {
        departmentService.removeUserFromDepartment(departmentId, userId);
    }
}