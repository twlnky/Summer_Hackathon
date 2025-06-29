package rut.miit.tech.summer_hackathon.domain.dto;

public record UserDTO(
        Long id,

        String username,

        String email,

        boolean isBanned,

        boolean isEnable) {}
