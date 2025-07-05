package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.Email;

public record UserDTO(
        Long id,

        String username,

        @Email
        String email,

        boolean isBanned,

        boolean isEnable) {}
