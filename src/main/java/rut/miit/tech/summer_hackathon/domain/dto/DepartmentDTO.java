package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepartmentDTO(
        @NotNull
        @Positive
        Long id,

        @NotBlank
        String name
) {}
