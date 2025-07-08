package rut.miit.tech.summer_hackathon.domain.dto;

import java.util.List;

public record SearchResponse(List<DepartmentDTO> departments,
                             List<UserDTO> users){
}
