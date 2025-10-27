package rut.miit.tech.summer_hackathon.service.department;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.DepartmentRepository;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Moderator createTestModerator(Long id, String login) {
        return Moderator.builder()
                .id(id)
                .login(login)
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .middleName("Smith")
                .departments(new ArrayList<>())
                .build();
    }

    private Department createTestDepartment(Long id, String name, Moderator moderator) {
        Department department = new Department();
        department.setId(id);
        department.setName(name);
        department.setModerator(moderator);
        return department;
    }

    private User createTestUser(Long id, String firstName, String middleName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        user.setDepartments(new ArrayList<>());
        return user;
    }

    private User createTestUser(Long id, String firstName) {
        return createTestUser(id, firstName, "Middle", "LastName");
    }

    @Test
    void update_WhenModeratorIsOwner_ShouldUpdateDepartment() {
        Long departmentId = 1L;
        Moderator moderator = createTestModerator(100L, "owner");
        Department existingDepartment = createTestDepartment(departmentId, "Old Name", moderator);

        Department updateData = new Department();
        updateData.setName("New Name");
        updateData.setModerator(moderator);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isModerator).thenReturn(true);

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(moderator);
            when(departmentRepository.save(any(Department.class))).thenReturn(existingDepartment);

            Department result = departmentService.update(departmentId, updateData);

            assertEquals("New Name", result.getName());
            assertEquals(moderator, result.getModerator());
            verify(departmentRepository).findById(departmentId);
            verify(departmentRepository).save(existingDepartment);
        }
    }

    @Test
    void update_WhenModeratorIsNotOwner_ShouldThrowAccessDenied() {
        Long departmentId = 1L;

        Moderator owner = createTestModerator(100L, "owner");
        Moderator otherModerator = createTestModerator(200L, "other");

        Department existingDepartment = createTestDepartment(departmentId, "Department", owner);
        Department updateData = new Department();

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isModerator).thenReturn(true);

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(otherModerator);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                departmentService.update(departmentId, updateData);
            });

            assertEquals("Cannot edit other departments", exception.getMessage());
            verify(departmentRepository, never()).save(any());
        }
    }

    @Test
    void update_WhenUserIsNotModerator_ShouldUpdateWithoutPermissionCheck() {
        Long departmentId = 1L;
        Moderator moderator = createTestModerator(100L, "moderator");
        Department existingDepartment = createTestDepartment(departmentId, "Old Name", moderator);

        Department updateData = new Department();
        updateData.setName("New Name");

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isModerator).thenReturn(false);

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
            when(departmentRepository.save(any(Department.class))).thenReturn(existingDepartment);

            Department result = departmentService.update(departmentId, updateData);

            assertEquals("New Name", result.getName());
            verify(securityUtils, never()).getAuthenticatedModerator();
        }
    }

    @Test
    void deleteById_ShouldRemoveUsersFromDepartmentAndDelete() {
        Long departmentId = 1L;
        Moderator moderator = createTestModerator(100L, "moderator");
        Department department = createTestDepartment(departmentId, "Test Department", moderator);

        User user1 = createTestUser(1L, "User1", "Middle1", "Last1");
        User user2 = createTestUser(2L, "User2", "Middle2", "Last2");

        user1.getDepartments().add(department);
        user2.getDepartments().add(department);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(user1, user2));

        departmentService.deleteById(departmentId);

        assertFalse(user1.getDepartments().contains(department));
        assertFalse(user2.getDepartments().contains(department));

        verify(userRepository, times(2)).save(any(User.class));
        verify(departmentRepository).deleteById(departmentId);
    }

    @Test
    void addUserToDepartment_ShouldAddUserToDepartment() {
        Long departmentId = 1L;
        Long userId = 1L;

        Moderator moderator = createTestModerator(100L, "moderator");
        Department department = createTestDepartment(departmentId, "Test Department", moderator);

        User user = createTestUser(userId, "Test", "User", "Name");

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        departmentService.addUserToDepartment(departmentId, userId);

        assertTrue(user.getDepartments().contains(department));
        verify(userRepository).save(user);
    }

    @Test
    void getById_WhenDepartmentExists_ShouldReturnDepartment() {
        Long departmentId = 1L;
        Moderator moderator = createTestModerator(100L, "moderator");
        Department expectedDepartment = createTestDepartment(departmentId, "Test Department", moderator);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(expectedDepartment));

        Department result = departmentService.getById(departmentId);

        assertEquals(expectedDepartment, result);
        verify(departmentRepository).findById(departmentId);
    }

    @Test
    void getById_WhenDepartmentNotExists_ShouldThrowException() {
        Long departmentId = 999L;
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departmentService.getById(departmentId);
        });

        assertTrue(exception.getMessage().contains("Department not found"));
    }
}