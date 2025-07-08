package rut.miit.tech.summer_hackathon.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

import java.util.Collections;
import java.util.List;

public record ModeratorDTO(
        Long id,

        @NotBlank(message = "Логин обязателен для заполнения")
        @Size(min = 3, max = 50, message = "Длина логина должна быть от 3 до 50 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин может содержать только буквы, цифры и подчеркивание")
        String login,

        @NotBlank(message = "Пароль обязателен для заполнения")
        @Size(min = 8, max = 100, message = "Длина пароля должна быть от 8 до 100 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,100}$",
                message = "Пароль должен содержать цифры, буквы в верхнем и нижнем регистре, специальные символы"
        )
        String password,

        @NotNull(message = "Список отделов не может быть null")
        List<Long> departmentsIds
) {

    public Moderator toModel() {
        List<Department> departments = departmentsIds != null ?
                departmentsIds.stream()
                        .map(departmentId -> {
                            Department dep = new Department();
                            dep.setId(departmentId);
                            return dep;
                        })
                        .toList() :
                Collections.emptyList();

        return new Moderator(
                id,
                login,
                password,
                departments
        );
    }
}