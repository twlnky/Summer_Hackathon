package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.*;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(name = "first_name", nullable = false)
    private String firstName;

    //@Column(name = "last_name", nullable = false)
    private String lastName;

    //@Column(name = "middle_name", nullable = false)
    private String middleName;

    @ManyToMany
    @JoinTable(
            name = "user_department",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private List<Department> departments = new ArrayList<>();

    @Column(name = "office_number")
    private Long officeNumber;

    //@Column(name = "business_phone", nullable = false)
    private String businessPhone;

    //@Column(name = "personal_phone", nullable = false)
    private String personalPhone;

    //@Column(nullable = false, unique = true)
    private String email;

    //@Column(name = "user_position", nullable = false)
    private String position;

    //@Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    public UserDTO toDto() {
        return new UserDTO(id,firstName,lastName,middleName,officeNumber,personalPhone,position,note,email,
                List.of());
    }
}