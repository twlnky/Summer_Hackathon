package rut.miit.tech.summer_hackathon.service.UserSearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserSearchServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSearchServiceImpl userSearchService;

    private User createTestUser(Long id, String firstName, String lastName, String email) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .personalPhone("+1234567890")
                .businessPhone("+0987654321")
                .position("Developer")
                .note("Test user")
                .officeNumber(101L)
                .build();
    }

    @Test
    void searchAllUsers_WithValidQuery_ShouldReturnMatchingUsers() {

        String query = "john";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> expectedUsers = List.of(
                createTestUser(1L, "John", "Doe", "john.doe@example.com"),
                createTestUser(2L, "Johnson", "Smith", "johnson@example.com")
        );
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(2, result.getQueryResult().size());
        assertEquals(expectedUsers, result.getQueryResult());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_WithEmptyQuery_ShouldReturnAllUsers() {

        String query = "";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> expectedUsers = List.of(
                createTestUser(1L, "John", "Doe", "john@example.com"),
                createTestUser(2L, "Jane", "Smith", "jane@example.com"),
                createTestUser(3L, "Bob", "Johnson", "bob@example.com")
        );
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(3, result.getQueryResult().size());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_WithBlankQuery_ShouldReturnAllUsers() {

        String query = "   ";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> expectedUsers = List.of(
                createTestUser(1L, "John", "Doe", "john@example.com")
        );
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getQueryResult().size());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_WithNoResults_ShouldReturnEmptyPage() {

        String query = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> expectedUsers = List.of();
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, 0);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertTrue(result.getQueryResult().isEmpty());
        assertEquals(0, result.getTotal());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_ShouldSearchInAllSpecifiedFields() {

        String query = "developer";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> expectedUsers = List.of(
                createTestUser(1L, "John", "Doe", "john@example.com")
        );
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getQueryResult().size());

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_WithDifferentPageable_ShouldUseCorrectPagination() {

        String query = "test";
        Pageable pageable = PageRequest.of(2, 5); // page 2, size 5

        List<User> expectedUsers = List.of(
                createTestUser(1L, "Test", "User", "test@example.com")
        );
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, 11); // всего 11 элементов

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getQueryResult().size());
        assertEquals(3, result.getPageCount()); // 11 элементов / 5 на страницу = 3 страницы
        assertEquals(5, result.getPageSize());
        assertEquals(11L, result.getTotal());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_WithCaseInsensitiveQuery_ShouldFindMatches() {

        String query = "JOHN";
        Pageable pageable = PageRequest.of(0, 10);

        List<User> expectedUsers = List.of(
                createTestUser(1L, "John", "Doe", "john@example.com"),
                createTestUser(2L, "johnny", "Smith", "johnny@example.com")
        );
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(2, result.getQueryResult().size());

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchAllUsers_WithPartialMatch_ShouldFindMatches() {

        String query = "dev";
        Pageable pageable = PageRequest.of(0, 10);

        User developerUser = createTestUser(1L, "John", "Doe", "john@example.com");
        developerUser.setPosition("Senior Developer");

        List<User> expectedUsers = List.of(developerUser);
        Page<User> expectedPage = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        PageResult<User> result = userSearchService.searchAllUsers(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getQueryResult().size());
        assertEquals("Senior Developer", result.getQueryResult().get(0).getPosition());

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}