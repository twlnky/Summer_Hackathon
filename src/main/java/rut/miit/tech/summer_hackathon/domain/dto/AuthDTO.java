package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthDTO(
        @NotBlank(message = "Поле имени не может быть пустым")
        String username,

        @NotBlank(message = "Поле пароля не может быть пустым")
        String password)
{
}
