package rut.miit.tech.summer_hackathon.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ErrorDTO(
        @NotBlank(message = "Код статуса не может быть пустым")
        String status,

        @NotBlank(message = "Сообщение об ошибке не может быть пустым")
        String message,

        @NotBlank(message = "Метка времени не может быть пустой")
        String date
) {}