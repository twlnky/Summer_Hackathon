package rut.miit.tech.summer_hackathon.controller.department;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.domain.dto.DepartmentDTO;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MODERATOR')")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public DepartmentDTO update(@PathVariable Long id, @Valid @RequestBody DepartmentDTO departmentDTO) {
        return departmentService.update(id, departmentDTO.toModel()).toDto();
    }
}
