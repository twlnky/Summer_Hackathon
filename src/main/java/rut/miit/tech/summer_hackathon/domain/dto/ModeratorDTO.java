package rut.miit.tech.summer_hackathon.domain.dto;


import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

import java.util.List;

public record ModeratorDTO(
        //TODO: Доделать валидацию модератора
        long id,
        String login,
        String password,
        List<Long> departmentsIds
) {

    public Moderator toModel(){
        return new Moderator(id, login, password, departmentsIds.stream()
                .map(s -> {
                    var dep = new Department();
                    dep.setId(dep.getId());
                    return dep;
                }).toList());
    }

}
