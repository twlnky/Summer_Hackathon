package rut.miit.tech.summer_hackathon.service.moderator;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.controller.moderator.ModeratorFilter;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.service.util.PageResult;


public interface ModeratorService {


    Moderator getById(Long id);


    Moderator save(Moderator moderator);


    void delete(Long id);


    Moderator update(Moderator moderator);


    PageResult<Moderator> getAll(ModeratorFilter filter, Pageable pageable);


    Moderator getByLogin(String username);
}