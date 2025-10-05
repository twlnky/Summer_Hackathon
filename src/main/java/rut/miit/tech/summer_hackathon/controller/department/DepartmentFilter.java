package rut.miit.tech.summer_hackathon.controller.department;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.Department;

public class DepartmentFilter implements Specification<Department> {

    private String name;

// N+1
    @Override
    public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String searchTerm = name.trim().toLowerCase();
        

        Predicate namePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + searchTerm + "%"
        );


        Predicate tagPredicate = criteriaBuilder.isMember(
                searchTerm,
                root.get("tags")
        );


        return criteriaBuilder.or(namePredicate, tagPredicate);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
