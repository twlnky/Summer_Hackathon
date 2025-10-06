package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@RequiredArgsConstructor
@Table
//Токен для логаута, тчобы отслеживать
public class RevokedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //Его id
    private Long id;

    @Setter
    //Id токена, который жвтИд
    @Column(nullable = false, unique = true)
    private String token;

}
