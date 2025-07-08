package rut.miit.tech.summer_hackathon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ModeratorRepository moderatorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        userRepository.deleteAll();
        departmentRepository.deleteAll();
        moderatorRepository.deleteAll();


        Moderator moderator1 = moderatorRepository.save(
                Moderator.builder()
                        .login("moder1")
                        .password(passwordEncoder.encode("moder1"))
                        .build()
        );

        Moderator moderator2 = moderatorRepository.save(
                Moderator.builder()
                        .login("moder2")
                        .password(passwordEncoder.encode("moder2"))
                        .build()
        );

        Department department1 = departmentRepository
                .save(
                        Department.builder()
                                .name("dep1")
                                .moderator(moderator1)
                                .build()
                );
        Department department2 = departmentRepository
                .save(
                        Department.builder()
                                .name("dep2")
                                .moderator(moderator2)
                                .build()
                );

        /*Department department1 = Department.builder()
                .id(3L)
                .build();

        Department department2 = Department.builder()
                .id(4L)
                .build();*/


        User user1 = userRepository.save(
                User.builder()
                        .email("email4")
                        .departments(List.of(department1, department2))
                        .build()
        );
        User user2 = userRepository.save(
                User.builder()
                        .email("email5")
                        .departments(List.of(department1, department2))
                        .build()
        );
        User user3 = userRepository.save(
                User.builder()
                        .email("email6")
                        .departments(List.of(department1, department2))
                        .build()
        );

    }
}
