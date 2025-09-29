package rut.miit.tech.summer_hackathon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.controller.query.PageParam;
import rut.miit.tech.summer_hackathon.controller.query.SortParam;
import rut.miit.tech.summer_hackathon.domain.dto.DepartmentDTO;
import rut.miit.tech.summer_hackathon.domain.dto.SearchResponse;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.user.UserService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {
    private final UserService userService;
    private final DepartmentService departmentService;


    @GetMapping()
    public SearchResponse search(@RequestParam String request,
                                 @ModelAttribute PageParam pageParam) {

        if(pageParam.getPage() == -1) {
            pageParam.setPage(0);
        }
        PageResult<UserDTO> users = userService.getAllByRequest(request, pageParam.toPageable(
                new SortParam(List.of("id:asc"))
                ))
                .map(User::toDto);
        PageResult<DepartmentDTO> departments = departmentService.getAllByRequest(request,
                        pageParam.toPageable(
                                new SortParam(List.of("id:asc"))
                        ))
                .map(Department::toDto);
        long pageCount = Math.max(users.getPageCount(), departments.getPageCount());
        return new SearchResponse(
                departments.getQueryResult(),
                users.getQueryResult(),
                pageCount
        );
    }
}