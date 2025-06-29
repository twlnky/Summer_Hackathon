package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 1, max = 20, message = "Поле имени должно быть " +
                "положительным и содержать не более 20 символов ")
        String username,

        @NotBlank(message = "Пароль не может быть пустым")
        String password,

        @NotBlank(message = "Неправильное сообщение")
        String confirmPassword,

        @NotBlank(message = "Поле почты не может быть пустым")
        @Email(message = "Почта не корректна")
        String email
) {}