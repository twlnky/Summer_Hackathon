package rut.miit.tech.summer_hackathon.repository;

import lombok.RequiredArgsConstructor;
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

        Department department1 = departmentRepository.save(
                Department.builder()
                        .name("dep1")
                        .tags(List.of("уит","аит","name"))
                        .moderator(moderator1)
                        .build()
        );

        Department department2 = departmentRepository.save(
                Department.builder()
                        .name("dep2")
                        .moderator(moderator2)
                        .tags(List.of("name"))
                        .build()
        );


        User user1 = userRepository.save(
                User.builder()
                        .firstName("name1")
                        .email("email4")
                        .departments(List.of(department1, department2))
                        .build()
        );

        User user2 = userRepository.save(
                User.builder()
                        .firstName("name2")
                        .email("email5")
                        .departments(List.of(department1, department2))
                        .moderator(moderator1)
                        .build()
        );
        for (int i = 0; i < 23; i++) {
            User user3 = userRepository.save(
                    User.builder()
                            .email("email6")
                            .moderator(moderator2)
                            .firstName("name" + i + 4)
                            .departments(List.of(department1, department2))
                            .build()
            );
        }

    }
}