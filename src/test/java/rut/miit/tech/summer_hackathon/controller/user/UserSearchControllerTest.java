package rut.miit.tech.summer_hackathon.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.UserSearch.UserSearchService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserSearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserSearchService userSearchService;

    @InjectMocks
    private UserSearchController userSearchController;

    private User testUser;
    private PageResult<User> userPageResult;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userSearchController).build();

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .position("Developer")
                .build();

        List<User> userList = List.of(testUser);
        userPageResult = new PageResult<>(
                userList, 1, 10, 1L
        );
    }

    @Test
    void searchUsers_WithValidQuery_ShouldReturnUsers() throws Exception {
        // Arrange
        String searchQuery = "john";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(1L))
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"))
                .andExpect(jsonPath("$.queryResult[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.queryResult[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.pageCount").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.total").value(1L));
    }

    @Test
    void searchUsers_WithEmptyQuery_ShouldReturnUsers() throws Exception {
        // Arrange
        String searchQuery = "";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").isArray())
                .andExpect(jsonPath("$.queryResult.length()").value(1));
    }

    @Test
    void searchUsers_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String searchQuery = "john-doe@example.com";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].email").value("john.doe@example.com"));
    }

    @Test
    void searchUsers_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        // Arrange
        String searchQuery = "test";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.queryResult").exists());
    }

    @Test
    void searchUsers_WithCustomPagination_ShouldUseCustomValues() throws Exception {
        // Arrange
        String searchQuery = "developer";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(10)) // Из PageResult
                .andExpect(jsonPath("$.queryResult").exists());
    }

    @Test
    void searchUsers_WithLongQuery_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String longQuery = "This is a very long search query that might contain multiple keywords for searching users";

        when(userSearchService.searchAllUsers(eq(longQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", longQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").exists());
    }

    @Test
    void searchUsers_WithNoResults_ShouldReturnEmptyList() throws Exception {
        // Arrange
        String searchQuery = "nonexistent";
        PageResult<User> emptyResult = new PageResult<>(List.of(), 0, 10, 0L);

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(emptyResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").isArray())
                .andExpect(jsonPath("$.queryResult.length()").value(0))
                .andExpect(jsonPath("$.total").value(0L))
                .andExpect(jsonPath("$.pageCount").value(0));
    }

    @Test
    void searchUsers_WithMultipleUsers_ShouldReturnAll() throws Exception {
        // Arrange
        String searchQuery = "engineer";

        User user1 = User.builder().id(1L).firstName("John").lastName("Doe").position("Software Engineer").build();
        User user2 = User.builder().id(2L).firstName("Jane").lastName("Smith").position("DevOps Engineer").build();
        User user3 = User.builder().id(3L).firstName("Bob").lastName("Johnson").position("Data Engineer").build();

        List<User> users = List.of(user1, user2, user3);
        PageResult<User> multiUserResult = new PageResult<>(users, 1, 10, 3L);

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(multiUserResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").isArray())
                .andExpect(jsonPath("$.queryResult.length()").value(3))
                .andExpect(jsonPath("$.total").value(3L))
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"))
                .andExpect(jsonPath("$.queryResult[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.queryResult[2].firstName").value("Bob"));
    }

    @Test
    void searchUsers_WithCaseInsensitiveQuery_ShouldWork() throws Exception {
        // Arrange
        String searchQuery = "JOHN";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"));
    }

    @Test
    void searchUsers_WithSpacesInQuery_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String searchQuery = "  john  doe  ";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].firstName").value("John"))
                .andExpect(jsonPath("$.queryResult[0].lastName").value("Doe"));
    }

    @Test
    void searchUsers_WithLargePageSize_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String searchQuery = "test";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").exists());
    }

    @Test
    void searchUsers_WithValidMinimalParameters_ShouldWork() throws Exception {
        // Arrange
        String searchQuery = "minimal";

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(userPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult").exists());
    }

    @Test
    void searchUsers_WithPartialUserData_ShouldReturnCorrectFields() throws Exception {
        // Arrange
        String searchQuery = "partial";
        User partialUser = User.builder()
                .id(5L)
                .firstName("Alice")
                .lastName("Brown")
                .build(); // email и position не установлены

        PageResult<User> partialResult = new PageResult<>(List.of(partialUser), 1, 10, 1L);

        when(userSearchService.searchAllUsers(eq(searchQuery), any(Pageable.class)))
                .thenReturn(partialResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].id").value(5L))
                .andExpect(jsonPath("$.queryResult[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.queryResult[0].lastName").value("Brown"));
    }
}