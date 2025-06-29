package rut.miit.tech.summer_hackathon.domain.dto;

import lombok.Builder;

@Builder
public record ErrorDTO(

        String status,

        String message,

        String date

) {
}
