package rut.miit.tech.summer_hackathon.controller.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rut.miit.tech.summer_hackathon.config.TestSecurityConfig;
import rut.miit.tech.summer_hackathon.controller.query.PageParam;
import rut.miit.tech.summer_hackathon.controller.query.SortParam;
import rut.miit.tech.summer_hackathon.controller.user.UserFilter;
import rut.miit.tech.summer_hackathon.domain.dto.DepartmentDTO;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.user.UserService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;

    private Department testDepartment;
    private DepartmentDTO testDepartmentDTO;
    private User testUser;
    private UserDTO testUserDTO;
    private PageResult<Department> departmentPageResult;
    private PageResult<User> userPageResult;

    @BeforeEach
    void setUp() {
        DepartmentController departmentController = new DepartmentController(departmentService, userService);

        // Используем TestSecurityConfig для настройки безопасности
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController)
                .defaultRequest(get("/").with(csrf())) // Добавляем CSRF по умолчанию
                .build();

        objectMapper = new ObjectMapper();

        // Setup test data с правильной структурой DepartmentDTO
        Moderator testModerator = Moderator.builder()
                .id(1L)
                .login("moderator1")
                .firstName("John")
                .lastName("Moderator")
                .middleName("Admin")
                .build();

        testDepartment = Department.builder()
                .id(1L)
                .name("IT Department")
                .moderator(testModerator)
                .build();

        testDepartmentDTO = new DepartmentDTO(
                1L,
                "IT Department",
                1L,
                "moderator1",
                "John",
                "Moderator",
                "Admin"
        );

        // Создаем пользователя с правильной структурой согласно классу User
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .middleName("Michael")
                .email("john.doe@example.com")
                .position("Developer")
                .businessPhone("+1234567890")
                .personalPhone("+0987654321")
                .officeNumber(101L)
                .note("Test user")
                .moderator(testModerator)
                .departments(new ArrayList<>(List.of(testDepartment))) // departments как List
                .build();

        testUserDTO = new UserDTO(
                1L, "John", "Doe", "Michael", 101L, "+0987654321",
                "Developer", "Test user", 1L, "john.doe@example.com", List.of(1L)
        );

        List<Department> departmentList = List.of(testDepartment);
        departmentPageResult = new PageResult<>(departmentList, 1, 10, 1L);

        List<User> userList = List.of(testUser);
        userPageResult = new PageResult<>(userList, 1, 10, 1L);
    }

    // Публичные endpoints - не требуют аутентификации

    @Test
    void getAllDepartmentsPublic_ShouldReturnDepartments() throws Exception {
        // Arrange
        when(departmentService.getAll(any(DepartmentFilter.class), any(Pageable.class)))
                .thenReturn(departmentPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/departments/public")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(1L))
                .andExpect(jsonPath("$.queryResult[0].name").value("IT Department"));
    }

    @Test
    void getUsersByDepartment_ShouldReturnUsers() throws Exception {
        // Arrange
        when(userService.getUserByDepartmentId(eq(1L), any(UserFilter.class), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/departments/public/1/users")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(1L))
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"));
    }

    @Test
    void getDepartmentById_ShouldReturnDepartment() throws Exception {
        // Arrange
        when(departmentService.getById(1L)).thenReturn(testDepartment);

        // Act & Assert
        mockMvc.perform(get("/api/v1/departments/public/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("IT Department"));
    }

    // Защищенные endpoints - требуют аутентификации и соответствующие роли

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createDepartment_WithAdminRole_ShouldCreateDepartment() throws Exception {
        // Arrange
        when(departmentService.save(any(Department.class))).thenReturn(testDepartment);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDepartmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("IT Department"));
    }


    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateDepartment_WithAdminRole_ShouldUpdateDepartment() throws Exception {
        // Arrange
        when(departmentService.update(eq(1L), any(Department.class))).thenReturn(testDepartment);

        // Act & Assert
        mockMvc.perform(put("/api/v1/departments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDepartmentDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("IT Department"));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void updateDepartment_WithModeratorRole_ShouldUpdateDepartment() throws Exception {
        // Arrange
        when(departmentService.update(eq(1L), any(Department.class))).thenReturn(testDepartment);

        // Act & Assert
        mockMvc.perform(put("/api/v1/departments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDepartmentDTO)))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void deleteDepartment_WithAdminRole_ShouldDeleteDepartment() throws Exception {
        // Arrange
        doNothing().when(departmentService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/departments/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void addUserToDepartment_WithAdminRole_ShouldAddUser() throws Exception {
        // Arrange
        doNothing().when(departmentService).addUserToDepartment(1L, 1L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments/1/users/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void addUserToDepartment_WithModeratorRole_ShouldAddUser() throws Exception {
        // Arrange
        doNothing().when(departmentService).addUserToDepartment(1L, 1L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments/1/users/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void removeUserFromDepartment_WithAdminRole_ShouldRemoveUser() throws Exception {
        // Arrange
        doNothing().when(departmentService).removeUserFromDepartment(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/departments/1/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void removeUserFromDepartment_WithModeratorRole_ShouldRemoveUser() throws Exception {
        // Arrange
        doNothing().when(departmentService).removeUserFromDepartment(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/departments/1/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createDepartment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange - создаем невалидный DTO с пустым именем
        DepartmentDTO invalidDepartmentDTO = new DepartmentDTO(
                null, "", 1L, "moderator1", "John", "Moderator", "Admin"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDepartmentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createDepartment_WithoutModerator_ShouldCreateDepartment() throws Exception {
        // Arrange
        DepartmentDTO departmentWithoutModerator = new DepartmentDTO(
                null, "HR Department", null, null, null, null, null
        );

        Department department = Department.builder()
                .id(2L)
                .name("HR Department")
                .moderator(null)
                .build();

        when(departmentService.save(any(Department.class))).thenReturn(department);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentWithoutModerator)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("HR Department"));
    }

    @Test
    void getUsersByDepartment_WithNonExistentDepartment_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(userService.getUserByDepartmentId(eq(999L), any(UserFilter.class), any(Pageable.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/departments/public/999/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").isArray())
                .andExpect(jsonPath("$.queryResult.length()").value(0))
                .andExpect(jsonPath("$.total").value(0L));
    }

    @Test
    void getDepartmentById_WithoutModerator_ShouldReturnDepartmentWithoutModeratorInfo() throws Exception {
        // Arrange
        Department departmentWithoutModerator = Department.builder()
                .id(2L)
                .name("HR Department")
                .moderator(null)
                .build();

        when(departmentService.getById(2L)).thenReturn(departmentWithoutModerator);

        // Act & Assert
        mockMvc.perform(get("/api/v1/departments/public/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("HR Department"))
                .andExpect(jsonPath("$.moderatorId").isEmpty())
                .andExpect(jsonPath("$.moderatorLogin").isEmpty())
                .andExpect(jsonPath("$.moderatorFirstName").isEmpty())
                .andExpect(jsonPath("$.moderatorLastName").isEmpty())
                .andExpect(jsonPath("$.moderatorMiddleName").isEmpty());
    }
}