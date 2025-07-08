package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import rut.miit.tech.summer_hackathon.domain.dto.DepartmentDTO;
import java.util.ArrayList;
import java.util.List;


@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "department", indexes = {
        @Index(name = "idx_department_id", columnList = "id"),
        @Index(name = "idx_department_name", columnList = "department_name"),
        @Index(name = "idx_department_moderator", columnList = "moderator_id")
})
public class Department {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_name")
    private String name;

    @Fetch(FetchMode.JOIN)
    @ManyToOne
    private Moderator moderator;

    @ManyToMany(mappedBy = "departments")
    private List<User> users = new ArrayList<>();


    @ElementCollection
    private List<String> tags;


    public DepartmentDTO toDto() {
        return new DepartmentDTO(
                id,
                name,
                moderator != null ? moderator.getId() : null
        );
    }
}