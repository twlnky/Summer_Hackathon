package rut.miit.tech.summer_hackathon.controller.user;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserFilter implements Specification<User> {
    private String firstName;
    private String lastName;
    private String middleName;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        String globalSearch = String.join(" ",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "",
                middleName != null ? middleName : ""
        ).trim();


        if (globalSearch.isEmpty()) {
            return cb.conjunction();
        }


        String[] searchTerms = globalSearch.split("\\s+");
        List<Predicate> predicates = new ArrayList<>();


        for (String term : searchTerms) {
            if (!term.isEmpty()) {
                String searchTerm = "%" + term.toLowerCase() + "%";
                predicates.add(createWordPredicate(root, cb, searchTerm));
            }
        }


        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate createWordPredicate(Root<User> root, CriteriaBuilder cb, String searchTerm) {
        List<Predicate> termPredicates = new ArrayList<>();

        String[] userFields = {
                "firstName", "lastName", "middleName",
                "businessPhone", "personalPhone", "email",
                "position", "note"
        };


        for (String field : userFields) {
            Expression<String> fieldLower = cb.lower(root.get(field));
            termPredicates.add(cb.like(fieldLower, searchTerm));
        }

        return cb.or(termPredicates.toArray(new Predicate[0]));
    }
}