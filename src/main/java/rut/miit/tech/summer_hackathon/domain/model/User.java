package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_users_first_name", columnList = "first_name"),
        @Index(name = "idx_users_last_name", columnList = "last_name"),
        @Index(name = "idx_users_middle_name", columnList = "middle_name"),
        @Index(name = "idx_users_office_number", columnList = "office_number"),
        @Index(name = "idx_users_business_phone", columnList = "business_phone"),
        @Index(name = "idx_users_personal_phone", columnList = "personal_phone"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_position", columnList = "position"),
        @Index(name = "idx_users_moderator_id", columnList = "moderator_id")
})

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    private String firstName;

    private String lastName;

    private String middleName;

    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "user_department",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private List<Department> departments = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @Fetch(FetchMode.JOIN)
    @ManyToOne
    @JoinColumn(name = "moderator_id", nullable = true)
    private Moderator moderator;

    @Column(name = "office_number")
    private Long officeNumber;

    private String businessPhone;
    private String personalPhone;
    private String email;
    private String position;
    private String note;

    public UserDTO toDto() {
        return new UserDTO(id, firstName, lastName, middleName, officeNumber, personalPhone, position, note,
                moderator == null ? null :
                        moderator.getId(), email,
                departments != null ? departments.stream().map(department -> department.getId()).toList() : List.of());
    }

}