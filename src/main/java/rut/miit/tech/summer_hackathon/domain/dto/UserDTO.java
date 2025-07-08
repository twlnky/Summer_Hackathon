package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.*;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;

import java.util.Collections;
import java.util.List;

public record UserDTO(
        Long id,

        @NotBlank(message = "Имя обязательно для заполнения")
        @Size(max = 50, message = "Имя не может превышать 50 символов")
        String firstName,

        @NotBlank(message = "Фамилия обязательна для заполнения")
        @Size(max = 50, message = "Фамилия не может превышать 50 символов")
        String lastName,

        @Size(max = 50, message = "Отчество не может превышать 50 символов")
        String middleName,

        @PositiveOrZero(message = "Номер офиса должен быть положительным числом")
        Long officeNumber,

        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,15}$", message = "Некорректный формат личного телефона")
        String personalPhone,

        @Size(max = 100, message = "Должность не может превышать 100 символов")
        String position,

        @Size(max = 500, message = "Примечание не может превышать 500 символов")
        String note,

        @NotNull(message = "ID модератора обязательно")
        Long moderatorId,

        @NotBlank(message = "Email обязателен для заполнения")
        @Email(message = "Некорректный формат email")
        @Size(max = 100, message = "Email не может превышать 100 символов")
        String email,

        List<Long> departmentsIds
) {

        public User toModel() {
                // Исправленная ошибка: было id вместо moderatorId
                Moderator moderator = Moderator.builder().id(moderatorId).build();

                List<Department> departments = departmentsIds != null ?
                        departmentsIds.stream()
                                .map(id -> Department.builder().id(id).build())
                                .toList() :
                        Collections.emptyList();

                return User.builder()
                        .id(id)
                        .firstName(firstName)
                        .lastName(lastName)
                        .middleName(middleName)
                        .officeNumber(officeNumber)
                        .personalPhone(personalPhone)
                        .position(position)
                        .note(note)
                        .moderator(moderator)
                        .email(email)
                        .departments(departments)
                        .build();
        }
}