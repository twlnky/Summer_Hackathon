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
        @Index(name = "idx_card_info", columnList = "last_name, first_name, middle_name, email, business_phone, personal_phone, position, office_number, moderator_id"),
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