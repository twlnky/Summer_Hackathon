package rut.miit.tech.summer_hackathon.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import rut.miit.tech.summer_hackathon.controller.user.UserFilter;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.security.SecurityUtils;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User createTestUser(Long id, String firstName, String lastName, Moderator moderator) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(firstName.toLowerCase() + "@example.com")
                .personalPhone("+1234567890")
                .businessPhone("+0987654321")
                .position("Developer")
                .note("Test user")
                .officeNumber(101L)
                .moderator(moderator)
                .departments(new ArrayList<>())
                .build();
    }

    private Moderator createTestModerator(Long id, String login) {
        return Moderator.builder()
                .id(id)
                .login(login)
                .password("password")
                .firstName("Mod")
                .lastName("Erator")
                .build();
    }

    private Department createTestDepartment(Long id, String name, Moderator moderator) {
        Department department = new Department();
        department.setId(id);
        department.setName(name);
        department.setModerator(moderator);
        return department;
    }

    @Test
    void save_ShouldSaveUser() {

        User userToSave = createTestUser(null, "John", "Doe", null);
        User savedUser = createTestUser(1L, "John", "Doe", null);

        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = userService.save(userToSave);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).save(userToSave);
    }

    @Test
    void getById_WhenUserExists_ShouldReturnUser() {

        Long userId = 1L;
        User expectedUser = createTestUser(userId, "John", "Doe", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getById(userId);

        assertEquals(expectedUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getById_WhenUserNotExists_ShouldThrowException() {

        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.getById(userId);
        });
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(userId);
    }

    @Test
    void getAll_ShouldReturnPageResult() {

        Specification<User> filter = new UserFilter();
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = List.of(
                createTestUser(1L, "John", "Doe", null),
                createTestUser(2L, "Jane", "Smith", null)
        );
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(filter, pageable)).thenReturn(page);

        PageResult<User> result = userService.getAll(filter, pageable);

        assertNotNull(result);
        assertEquals(2, result.getQueryResult().size());
        verify(userRepository).findAll(filter, pageable);
    }

    @Test
    void update_WhenUserIsAdmin_ShouldUpdateWithoutRestrictions() {

        Long userId = 1L;
        User existingUser = createTestUser(userId, "Old", "User", null);
        User updateData = createTestUser(userId, "New", "User", null);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(updateData)).thenReturn(updateData);

            User result = userService.update(userId, updateData);

            assertEquals(updateData, result);
            verify(userRepository).save(updateData);
            verify(securityUtils, never()).getAuthenticatedModerator();
        }
    }

    @Test
    void update_WhenUserIsMainModerator_ShouldUpdate() {

        Long userId = 1L;
        Moderator moderator = createTestModerator(100L, "main");
        User existingUser = createTestUser(userId, "Old", "User", moderator);
        User updateData = createTestUser(userId, "New", "User", moderator);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(moderator);
            when(userRepository.save(updateData)).thenReturn(updateData);

            User result = userService.update(userId, updateData);

            assertEquals(updateData, result);
            verify(userRepository).save(updateData);
        }
    }

    @Test
    void update_WhenUserHasNoModerator_ShouldThrowAccessDenied() {

        Long userId = 1L;
        User existingUser = createTestUser(userId, "Old", "User", null);
        User updateData = createTestUser(userId, "New", "User", null);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                userService.update(userId, updateData);
            });
            assertEquals("Cannot update user, because you're not legal moderator", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void update_WhenNotMainModeratorTriesToChangeAttributes_ShouldThrowAccessDenied() {

        Long userId = 1L;
        Moderator mainModerator = createTestModerator(100L, "main");
        Moderator otherModerator = createTestModerator(200L, "other");

        User existingUser = createTestUser(userId, "Old", "User", mainModerator);
        User updateData = createTestUser(userId, "New", "User", mainModerator); // Изменено имя

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(otherModerator);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                userService.update(userId, updateData);
            });
            assertEquals("Cannot update user attributes, because you're not main moderator", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void update_WhenNotMainModeratorTriesToChangeModerator_ShouldThrowAccessDenied() {

        Long userId = 1L;
        Moderator mainModerator = createTestModerator(100L, "main");
        Moderator otherModerator = createTestModerator(200L, "other");
        Moderator newModerator = createTestModerator(300L, "new");

        User existingUser = createTestUser(userId, "User", "User", mainModerator);

        User updateData = User.builder()
                .id(userId)
                .firstName("User")
                .lastName("User")
                .email("user@example.com")
                .personalPhone("+1234567890")
                .businessPhone("+0987654321")
                .position("Developer")
                .note("Test user")
                .officeNumber(101L)
                .moderator(newModerator) // Другой модератор!
                .departments(new ArrayList<>())
                .build();

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(otherModerator);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                userService.update(userId, updateData);
            });

            assertTrue(exception.getMessage().contains("Cannot update user's moderator") ||
                    exception.getMessage().contains("Cannot update user attributes"));
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void delete_WhenUserIsAdmin_ShouldDeleteWithoutRestrictions() {

        Long userId = 1L;
        User user = createTestUser(userId, "John", "Doe", null);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userService.delete(userId);

            verify(userRepository).deleteById(userId);
            verify(securityUtils, never()).getAuthenticatedModerator();
        }
    }

    @Test
    void delete_WhenUserIsMainModerator_ShouldDelete() {

        Long userId = 1L;
        Moderator moderator = createTestModerator(100L, "main");
        User user = createTestUser(userId, "John", "Doe", moderator);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(moderator);

            userService.delete(userId);

            verify(userRepository).deleteById(userId);
        }
    }

    @Test
    void delete_WhenUserHasNoModerator_ShouldThrowAccessDenied() {

        Long userId = 1L;
        User user = createTestUser(userId, "John", "Doe", null);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                userService.delete(userId);
            });
            assertEquals("Cannot delete user, because you're not legal moderator", exception.getMessage());
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Test
    void delete_WhenNotMainModerator_ShouldThrowAccessDenied() {

        Long userId = 1L;
        Moderator mainModerator = createTestModerator(100L, "main");
        Moderator otherModerator = createTestModerator(200L, "other");
        User user = createTestUser(userId, "John", "Doe", mainModerator);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(otherModerator);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                userService.delete(userId);
            });
            assertEquals("Cannot delete user, because you're not main moderator", exception.getMessage());
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Test
    void getUserByDepartmentId_ShouldReturnUsersFromDepartment() {

        Long departmentId = 1L;
        Specification<User> filter = new UserFilter();
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = List.of(
                createTestUser(1L, "John", "Doe", null),
                createTestUser(2L, "Jane", "Smith", null)
        );
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResult<User> result = userService.getUserByDepartmentId(departmentId, filter, pageable);

        assertNotNull(result);
        assertEquals(2, result.getQueryResult().size());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllByRequest_WithBlankRequest_ShouldUseDefaultFilter() {

        String request = "   ";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = List.of(createTestUser(1L, "John", "Doe", null));
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResult<User> result = userService.getAllByRequest(request, pageable);

        assertNotNull(result);
        assertEquals(1, result.getQueryResult().size());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllByRequest_WithSearchRequest_ShouldUseSearchFilter() {

        String request = "john";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = List.of(createTestUser(1L, "John", "Doe", null));
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResult<User> result = userService.getAllByRequest(request, pageable);

        assertNotNull(result);
        assertEquals(1, result.getQueryResult().size());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllByRequest_WithNumericRequest_ShouldSearchOfficeNumber() {

        String request = "101";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = List.of(createTestUser(1L, "John", "Doe", null));
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResult<User> result = userService.getAllByRequest(request, pageable);

        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void update_WhenMainModeratorChangesDepartments_ShouldUpdate() {

        Long userId = 1L;
        Moderator mainModerator = createTestModerator(100L, "main");

        User existingUser = createTestUser(userId, "User", "User", mainModerator);
        User updateData = createTestUser(userId, "User", "User", mainModerator);

        Department mainModeratorDept = createTestDepartment(1L, "Main Dept", mainModerator);
        Department otherDept = createTestDepartment(2L, "Other Dept", mainModerator);

        existingUser.setDepartments(new ArrayList<>(List.of(mainModeratorDept)));

        updateData.setDepartments(new ArrayList<>(List.of(mainModeratorDept, otherDept)));

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(mainModerator);
            when(userRepository.save(updateData)).thenReturn(updateData);

            User result = userService.update(userId, updateData);

            assertEquals(updateData, result);
            verify(userRepository).save(updateData);
        }
    }

    @Test
    void update_WhenNotMainModeratorModifiesOnlyOwnDepartments_ShouldUpdate() {

        Long userId = 1L;
        Moderator mainModerator = createTestModerator(100L, "main");
        Moderator otherModerator = createTestModerator(200L, "other");

        User existingUser = createTestUser(userId, "User", "User", mainModerator);

        User updateData = createTestUser(userId, "User", "User", mainModerator);

        Department mainModeratorDept = createTestDepartment(1L, "Main Dept", mainModerator);
        Department otherModeratorDept = createTestDepartment(2L, "Other Dept", otherModerator);

        existingUser.setDepartments(new ArrayList<>(List.of(mainModeratorDept, otherModeratorDept)));

        updateData.setDepartments(new ArrayList<>(List.of(mainModeratorDept, otherModeratorDept)));

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(securityUtils.getAuthenticatedModerator()).thenReturn(otherModerator);

            when(departmentService.getAllDepartmentsByUserId(userId))
                    .thenReturn(List.of(mainModeratorDept, otherModeratorDept));

            when(userRepository.save(updateData)).thenReturn(updateData);

            User result = userService.update(userId, updateData); // TODO

            assertEquals(updateData, result);
            verify(userRepository).save(updateData);
        }
    }
}