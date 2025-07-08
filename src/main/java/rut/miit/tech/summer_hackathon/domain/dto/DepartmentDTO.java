package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

import java.util.List;

public record DepartmentDTO(
        @NotNull
        @Positive
        Long id,
        @NotBlank
        String name,
        Long moderatorId
) {
        public Department toModel(){
                return Department.builder()
                        .id(id)
                        .name(name)
                        .moderator(Moderator.builder()
                                .id(moderatorId)
                                .build())
                        .build();
        }
}
