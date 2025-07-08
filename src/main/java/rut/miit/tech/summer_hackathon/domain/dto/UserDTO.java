package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.Email;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;

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
        @Email
        String email,
        List<Long> departmentsIds) {

        User toModel() {
                return User.builder()
                        .id(id)
                        .firstName(firstName)
                        .lastName(lastName)
                        .middleName(middleName)
                        .officeNumber(officeNumber)
                        .personalPhone(personalPhone)
                        .position(position)
                        .note(note)
                        .email(email)
                        .departments(
                                departmentsIds.stream().map(
                                        id -> Department.builder()
                                                .id(id)
                                                .build()
                                ).toList()
                        )
                        .build();
        }
}
