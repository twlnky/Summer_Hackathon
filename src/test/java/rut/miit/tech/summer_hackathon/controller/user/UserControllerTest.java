package rut.miit.tech.summer_hackathon.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.user.UserService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        }
)
@Import(UserControllerTest.TestSecurityConfig.class) // Явно импортируем тестовую конфигурацию
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private PageResult<User> userPageResult;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .position("Developer")
                .build();

        testUserDTO = new UserDTO(
                1L, "John", "Doe", "Michael", 101L, "+1234567890",
                "Developer", "Test user", 1L, "john.doe@example.com", List.of(1L, 2L)
        );

        List<User> userList = List.of(testUser);
        userPageResult = new PageResult<>(
                userList, 1, 10, 1L
        );
    }

    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz
                            // Разрешаем ВСЕ запросы для тестов
                            .anyRequest().permitAll()
                    )
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

    @Test
    void getAllUsersPublic_ShouldReturnUsers() throws Exception {
        // Arrange
        Mockito.when(userService.getAll(any(), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/public")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(1L))
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"))
                .andExpect(jsonPath("$.queryResult[0].lastName").value("Doe"));
    }

    @Test
    void getAllUsersPublic_WithEmptyFilter_ShouldReturnAllUsers() throws Exception {
        // Arrange
        Mockito.when(userService.getAll(any(), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/public")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").isArray())
                .andExpect(jsonPath("$.queryResult.length()").value(1));
    }

    @Test
    void getAllUsersPrivate_ShouldReturnUsers() throws Exception {
        // Arrange
        Mockito.when(userService.getAll(any(), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert - теперь приватный endpoint тоже доступен без аутентификации
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(1L))
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        // Arrange
        Mockito.when(userService.getById(1L)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/public/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }


    @Test
    void getUserById_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Mockito.when(userService.getById(999L))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/public/999"))
                .andExpect(status().isNotFound())  // 404, т.к. handleNotFoundException обрабатывает RuntimeException
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.date").exists()); // проверяем, что дата присутствует
    }


    @Test
    void createUser_ShouldCreateUser() throws Exception {
        // Arrange
        Mockito.when(userService.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void updateUser_ShouldUpdateUser() throws Exception {
        // Arrange
        Mockito.when(userService.update(eq(1L), any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void deleteUser_ShouldDeleteUser() throws Exception {
        // Arrange
        Mockito.doNothing().when(userService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void getAllUsersPrivate_WithModeratorRole_ShouldReturnUsersWithAuth() throws Exception {
        // Arrange
        Mockito.when(userService.getAll(any(), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(1L));
    }
}