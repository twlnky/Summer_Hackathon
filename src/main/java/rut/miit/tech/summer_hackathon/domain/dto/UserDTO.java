package rut.miit.tech.summer_hackathon.domain.dto;

import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;

import java.util.Collections;
import java.util.List;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        Long officeNumber,
        String personalPhone,
        String position,
        String note,
        Long moderatorId,
        String email,
        List<Long> departmentsIds
) {

    public User toModel() {

        Moderator moderator = null;
        if (moderatorId != null) {
            moderator = Moderator.builder().id(moderatorId).build();
        }

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