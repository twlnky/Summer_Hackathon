package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String username;

    @Setter
    private String password;

    @Setter
    private boolean isBanned;

    @Setter
    private boolean isEnable;

    @Setter
    private String email;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class)
    @Column(name = "roles")
    private List<Role> roles;

}

