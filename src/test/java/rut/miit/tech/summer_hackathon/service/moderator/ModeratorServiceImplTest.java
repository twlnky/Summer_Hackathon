package rut.miit.tech.summer_hackathon.service.moderator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import rut.miit.tech.summer_hackathon.controller.moderator.ModeratorFilter;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.repository.ModeratorRepository;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModeratorServiceImplTest {

    @Mock
    private ModeratorRepository moderatorRepository;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ModeratorServiceImpl moderatorService;

    private Moderator createTestModerator(Long id, String login, String password) {
        return Moderator.builder()
                .id(id)
                .login(login)
                .password(password)
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

    @Test
    void getById_WhenModeratorExists_ShouldReturnModerator() {
        Long moderatorId = 1L;
        Moderator expectedModerator = createTestModerator(moderatorId, "testuser", "password");

        when(moderatorRepository.findById(moderatorId)).thenReturn(Optional.of(expectedModerator));

        Moderator result = moderatorService.getById(moderatorId);

        assertEquals(expectedModerator, result);
        verify(moderatorRepository).findById(moderatorId);
    }

    @Test
    void getById_WhenModeratorNotExists_ShouldThrowException() {

        Long moderatorId = 999L;
        when(moderatorRepository.findById(moderatorId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            moderatorService.getById(moderatorId);
        });
        verify(moderatorRepository).findById(moderatorId);
    }

    @Test
    void save_ShouldEncodePasswordAndSetDepartments() {

        String rawPassword = "plainPassword";
        String encodedPassword = "encodedPassword";

        Moderator moderatorToSave = createTestModerator(null, "newuser", rawPassword);

        Department dep1 = createTestDepartment(1L, "Dept 1", null);
        Department dep2 = createTestDepartment(2L, "Dept 2", null);
        moderatorToSave.setDepartments(new ArrayList<>(List.of(dep1, dep2)));

        Moderator savedModerator = createTestModerator(1L, "newuser", encodedPassword);
        savedModerator.setDepartments(new ArrayList<>(List.of(dep1, dep2)));

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(departmentService.getAllByIds(List.of(1L, 2L))).thenReturn(List.of(dep1, dep2));
        when(moderatorRepository.save(moderatorToSave)).thenReturn(savedModerator);

        Moderator result = moderatorService.save(moderatorToSave);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newuser", result.getLogin());
        verify(passwordEncoder).encode(rawPassword);
        verify(departmentService).getAllByIds(List.of(1L, 2L));
        verify(departmentService).saveAll(List.of(dep1, dep2));

        assertEquals(savedModerator, dep1.getModerator());
        assertEquals(savedModerator, dep2.getModerator());
    }

    @Test
    void save_WhenNoDepartments_ShouldNotCallDepartmentServiceForEmptyLists() {

        Moderator moderatorToSave = createTestModerator(null, "newuser", "password");
        moderatorToSave.setDepartments(new ArrayList<>()); // Пустой изменяемый список

        Moderator savedModerator = createTestModerator(1L, "newuser", "encodedPassword");
        savedModerator.setDepartments(new ArrayList<>());

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(moderatorRepository.save(moderatorToSave)).thenReturn(savedModerator);

        Moderator result = moderatorService.save(moderatorToSave);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(departmentService).getAllByIds(List.of());
        verify(departmentService).saveAll(List.of());
    }

    @Test
    void delete_ShouldCallRepository() {

        Long moderatorId = 1L;

        moderatorService.delete(moderatorId);

        verify(moderatorRepository).deleteById(moderatorId);
    }

    @Test
    void update_ShouldReassignDepartmentsCorrectly() {

        Long moderatorId = 1L;

        Moderator existingModerator = createTestModerator(moderatorId, "existing", "oldPassword");
        Department oldDep1 = createTestDepartment(1L, "Old Dept 1", existingModerator);
        Department oldDep2 = createTestDepartment(2L, "Old Dept 2", existingModerator);
        existingModerator.setDepartments(new ArrayList<>(List.of(oldDep1, oldDep2)));

        Moderator updateData = createTestModerator(moderatorId, "updated", "newPassword");
        Department newDep2 = createTestDepartment(2L, "Updated Dept 2", null); // Остается
        Department newDep3 = createTestDepartment(3L, "New Dept 3", null); // Новый
        updateData.setDepartments(new ArrayList<>(List.of(newDep2, newDep3)));

        when(moderatorRepository.findById(moderatorId)).thenReturn(Optional.of(existingModerator));
        when(departmentService.getAllByModeratorId(moderatorId)).thenReturn(new ArrayList<>(List.of(oldDep1, oldDep2)));
        when(departmentService.getAllByIds(List.of(2L, 3L))).thenReturn(new ArrayList<>(List.of(newDep2, newDep3)));
        when(departmentService.updateAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(moderatorRepository.save(updateData)).thenReturn(updateData);

        Moderator result = moderatorService.update(updateData);

        assertEquals(updateData, result);

        assertEquals("oldPassword", updateData.getPassword());


        assertNull(oldDep1.getModerator());

        assertEquals(updateData, newDep2.getModerator());
        assertEquals(updateData, newDep3.getModerator());

        verify(departmentService).getAllByModeratorId(moderatorId);
        verify(departmentService).getAllByIds(List.of(2L, 3L));
        verify(departmentService).updateAll(anyList());
        verify(moderatorRepository).save(updateData);
    }

    @Test
    void update_WhenAllDepartmentsRemoved_ShouldUnlinkAll() {

        Long moderatorId = 1L;

        Moderator existingModerator = createTestModerator(moderatorId, "existing", "password");
        Department oldDep1 = createTestDepartment(1L, "Old Dept 1", existingModerator);
        Department oldDep2 = createTestDepartment(2L, "Old Dept 2", existingModerator);
        existingModerator.setDepartments(new ArrayList<>(List.of(oldDep1, oldDep2)));

        Moderator updateData = createTestModerator(moderatorId, "updated", "newPassword");
        updateData.setDepartments(new ArrayList<>()); // Убираем все департаменты

        when(moderatorRepository.findById(moderatorId)).thenReturn(Optional.of(existingModerator));
        when(departmentService.getAllByModeratorId(moderatorId)).thenReturn(new ArrayList<>(List.of(oldDep1, oldDep2)));
        when(departmentService.getAllByIds(List.of())).thenReturn(new ArrayList<>());
        when(departmentService.updateAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(moderatorRepository.save(updateData)).thenReturn(updateData);

        Moderator result = moderatorService.update(updateData);

        assertEquals(updateData, result);

        assertNull(oldDep1.getModerator());
        assertNull(oldDep2.getModerator());

        verify(departmentService).updateAll(List.of(oldDep1, oldDep2));
    }

    @Test
    void update_WhenAllDepartmentsChanged_ShouldRelinkAll() {

        Long moderatorId = 1L;

        Moderator existingModerator = createTestModerator(moderatorId, "existing", "password");
        Department oldDep1 = createTestDepartment(1L, "Old Dept 1", existingModerator);
        existingModerator.setDepartments(new ArrayList<>(List.of(oldDep1)));

        Moderator updateData = createTestModerator(moderatorId, "updated", "newPassword");
        Department newDep2 = createTestDepartment(2L, "New Dept 2", null);
        Department newDep3 = createTestDepartment(3L, "New Dept 3", null);
        updateData.setDepartments(new ArrayList<>(List.of(newDep2, newDep3)));

        when(moderatorRepository.findById(moderatorId)).thenReturn(Optional.of(existingModerator));
        when(departmentService.getAllByModeratorId(moderatorId)).thenReturn(new ArrayList<>(List.of(oldDep1)));
        when(departmentService.getAllByIds(List.of(2L, 3L))).thenReturn(new ArrayList<>(List.of(newDep2, newDep3)));
        when(departmentService.updateAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(moderatorRepository.save(updateData)).thenReturn(updateData);

        Moderator result = moderatorService.update(updateData);

        assertEquals(updateData, result);

        assertNull(oldDep1.getModerator());

        assertEquals(updateData, newDep2.getModerator());
        assertEquals(updateData, newDep3.getModerator());
    }

    @Test
    void getAll_ShouldReturnPageResult() {

        ModeratorFilter filter = new ModeratorFilter();
        Pageable pageable = PageRequest.of(0, 10);

        List<Moderator> moderators = List.of(
                createTestModerator(1L, "user1", "pass1"),
                createTestModerator(2L, "user2", "pass2")
        );

        Page<Moderator> page = new PageImpl<>(moderators, pageable, 2L);

        when(moderatorRepository.findAll(any(), any(), eq(pageable))).thenReturn(page);

        PageResult<Moderator> result = moderatorService.getAll(filter, pageable);

        assertNotNull(result);

        assertEquals(2, result.getQueryResult().size());
        assertEquals(moderators, result.getQueryResult());

        assertEquals(1, result.getPageCount()); // 2 элемента / 10 на страницу = 1 страница
        assertEquals(10, result.getPageSize()); // размер страницы
        assertEquals(2L, result.getTotal()); // всего элементов

        verify(moderatorRepository).findAll(any(), any(), eq(pageable));
    }

    @Test
    void getAll_WithEmptyResult_ShouldReturnEmptyPageResult() {

        ModeratorFilter filter = new ModeratorFilter();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Moderator> emptyPage = new PageImpl<>(List.of(), pageable, 0L);

        when(moderatorRepository.findAll(any(), any(), eq(pageable))).thenReturn(emptyPage);

        PageResult<Moderator> result = moderatorService.getAll(filter, pageable);

        assertNotNull(result);
        assertTrue(result.getQueryResult().isEmpty());
        assertEquals(0, result.getPageCount());
        assertEquals(10, result.getPageSize());
        assertEquals(0L, result.getTotal());
    }

    @Test
    void getByLogin_WhenModeratorExists_ShouldReturnModerator() {

        String login = "testuser";
        Moderator expectedModerator = createTestModerator(1L, login, "password");

        when(moderatorRepository.findByLogin(login)).thenReturn(Optional.of(expectedModerator));

        Moderator result = moderatorService.getByLogin(login);

        assertEquals(expectedModerator, result);
        verify(moderatorRepository).findByLogin(login);
    }

    @Test
    void getByLogin_WhenModeratorNotExists_ShouldThrowException() {

        String login = "nonexistent";
        when(moderatorRepository.findByLogin(login)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            moderatorService.getByLogin(login);
        });
        verify(moderatorRepository).findByLogin(login);
    }
}