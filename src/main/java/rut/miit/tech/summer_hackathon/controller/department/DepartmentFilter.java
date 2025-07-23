package rut.miit.tech.summer_hackathon.controller.department;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.Department;

public class DepartmentFilter implements Specification<Department> {
    
    private String name;
    
    @Override
    public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        // Если поисковый запрос пустой, возвращаем null (показать все)
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        String searchTerm = name.trim().toLowerCase();
        
        // Поиск по названию департамента
        Predicate namePredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("name")), 
            "%" + searchTerm + "%"
        );
        
        // Поиск по тегам
        Predicate tagPredicate = criteriaBuilder.isMember(
            searchTerm, 
            root.get("tags")
        );
        
        // Возвращаем OR условие: найти если есть совпадение в названии ИЛИ в тегах
        return criteriaBuilder.or(namePredicate, tagPredicate);
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
