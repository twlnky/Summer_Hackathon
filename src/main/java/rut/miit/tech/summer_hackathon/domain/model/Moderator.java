package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.*;
import rut.miit.tech.summer_hackathon.domain.dto.ModeratorDTO;

import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "moderator", indexes = {
        @Index(name = "idx_moderator_id", columnList = "id"),
        @Index(name = "idx_moderator_login", columnList = "login"),
        @Index(name = "idx_moderator_password", columnList = "password")
})
public class Moderator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String login;

    @Setter
    private String password;

    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moderator")
    private List<Department> departments;

    public ModeratorDTO toDto(){
        return new ModeratorDTO(id,login,password, departments != null && !departments.isEmpty() ? departments.stream().map(Department::getId).toList() : List.of());
    }

}
