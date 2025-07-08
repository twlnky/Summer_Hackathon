package rut.miit.tech.summer_hackathon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import rut.miit.tech.summer_hackathon.domain.dto.SearchResponse;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.user.UserService;

@RestController("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final UserService userService;
    private final DepartmentService departmentService;

    @GetMapping("/{request}")
    public SearchResponse search(@PathVariable String request){
        return new SearchResponse(departmentService.getAllByRequest(request)
                                .stream().map(Department::toDto)
                                .toList(),
                userService.getAllByRequest(request).stream()
                        .map(User::toDto).toList());
    }



}
