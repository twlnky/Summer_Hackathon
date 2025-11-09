package rut.miit.tech.summer_hackathon.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Отключаем CSRF для тестов
                .authorizeHttpRequests(authz -> authz
                        // Разрешаем доступ к публичным endpoints без аутентификации
                        .requestMatchers("/api/v1/users/public/**").permitAll()
                        // Все остальные endpoints требуют аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)  // Отключаем Basic Auth
                .formLogin(AbstractHttpConfigurer::disable); // Отключаем форму логина

        return http.build();
    }
}
