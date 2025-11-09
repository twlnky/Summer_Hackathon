package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.*;
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
        @Index(name = "idx_department_moderator_connection", columnList = "department_name, moderator_id")
})
public class Department {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private Moderator moderator;

    @ManyToMany(mappedBy = "departments")
    private List<User> users = new ArrayList<>();


    @ElementCollection
    private List<String> tags;


    public DepartmentDTO toDto() {
        return new DepartmentDTO(
                id,
                name,
                moderator != null ? moderator.getId() : null,
                moderator != null ? moderator.getLogin() : null,
                moderator != null ? moderator.getFirstName() : null,
                moderator != null ? moderator.getLastName() : null,
                moderator != null ? moderator.getMiddleName() : null
        );
    }
}