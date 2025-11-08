package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.NotBlank;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

public record DepartmentDTO(
        Long id,

        @NotBlank
        String name,

        Long moderatorId,


        String moderatorLogin,
        String moderatorFirstName,
        String moderatorLastName,
        String moderatorMiddleName
) {

    public Department toModel() {
        return Department.builder()
                .id(id)
                .name(name)
                .moderator(
                        moderatorId != null ?
                                Moderator.builder().id(moderatorId).build() :
                                null
                )
                .build();
    }
}