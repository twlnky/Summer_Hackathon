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

    //TODO: Реализовать недостоящие поля модели User в соответсвии с базой данных
    /*
    * Поля
    * last_name varchar(255)
    * middle_name varchar(255)
    * реализиовать связь сотрудника с его департаментом(department_id(long)) (какая это связь? (Явно укажи фетч лейзи))
    * business_phone varchar(255) внимательно с валидацией номер должен валидироваться(не слишком длинный содержать опреденные знаки(посмотри про regex))
    * personal_phone varchar(255) внимательно с валидацией номер должен валидироваться(не слишком длинный содержать опреденные знаки(посмотри про regex))
    * user_position varchar(255)
    * note
    * user_role взять и связать с енамом Role
    * свяжи password и user_password @JoinColumn
    * свяжи username и user_username @JoinColumn
    * Валидитурй в DTO
    * */

}

