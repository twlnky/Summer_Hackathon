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
        List<String> searchTerms = new ArrayList<>();
        
        // Собираем все непустые поисковые термы
        if (firstName != null && !firstName.trim().isEmpty()) {
            searchTerms.add(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            searchTerms.add(lastName.trim());
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            searchTerms.add(middleName.trim());
        }

        if (searchTerms.isEmpty()) {
            return cb.conjunction();
        }

        List<Predicate> predicates = new ArrayList<>();

        // Для каждого поискового терма создаем предикат поиска по всем полям
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
            try {
                Expression<String> fieldLower = cb.lower(root.get(field));
                termPredicates.add(cb.like(fieldLower, searchTerm));
            } catch (Exception e) {
                // Игнорируем поля, которые могут быть null или недоступны
            }
        }

        // Также ищем по номеру кабинета как строке
        try {
            Expression<String> officeNumberStr = cb.toString(root.get("officeNumber"));
            termPredicates.add(cb.like(officeNumberStr, searchTerm.replace("%", "")));
        } catch (Exception e) {
            // Игнорируем если поле недоступно
        }

        return cb.or(termPredicates.toArray(new Predicate[0]));
    }

    // Геттеры и сеттеры для Spring автоматического связывания параметров
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
}