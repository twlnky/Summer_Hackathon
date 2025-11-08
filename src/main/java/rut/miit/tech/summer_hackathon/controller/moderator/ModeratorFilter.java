package rut.miit.tech.summer_hackathon.controller.moderator;

import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.controller.query.AbstractFilter;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import java.util.ArrayList;
import java.util.List;

@Data
public class ModeratorFilter
        extends AbstractFilter
        implements Specification<Moderator> {
    private String login;

    @Override
    public Predicate toPredicate(Root<Moderator> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if(login != null) {

            predicates.add(cb.like(root.get("login"), "%" + login + "%"));
        }
        applyJoins(root);

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    public ModeratorFilter copy(){
        ModeratorFilter moderatorFilter = new ModeratorFilter();
        moderatorFilter.login = this.login;
        return moderatorFilter;
    }
}