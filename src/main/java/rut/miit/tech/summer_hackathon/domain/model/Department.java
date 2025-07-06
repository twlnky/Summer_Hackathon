package rut.miit.tech.summer_hackathon.domain.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

public class Department {
    //TODO: реализовать сущность Department

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "department_name")
    private String name;

    /*
    * реализуй сущность Department
    * Model DTO(с валидацией)
    * DepartmentController(внимательно изучи роутинг проекта)
    * DepartmentService и его имплиментацию
    * DepartmentRepository
    * изучай код проекта и делай по аналогии
    * */
}
