package rut.miit.tech.summer_hackathon.service.user;

import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.domain.model.User;

public interface UserService {
    User getById(Long id);
    User getByUsername(String username);
    User save(RegisterDTO registerDTO);

}
