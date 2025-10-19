package rut.miit.tech.summer_hackathon.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rut.miit.tech.summer_hackathon.controller.query.PageParam;
import rut.miit.tech.summer_hackathon.domain.dto.DepartmentDTO;
import rut.miit.tech.summer_hackathon.domain.dto.SearchResponse;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.user.UserService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SearchControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearch_ValidRequest() {
        // given
        String request = "john";
        PageParam pageParam = new PageParam();
        pageParam.setPage(1);
        pageParam.setSize(10);

        UserDTO userDTO = new UserDTO(
                1L, "john@example.com", "John", "Doe", null,
                "USER", "John", "1234567890", null, "ACTIVE", List.of()
        );

        DepartmentDTO deptDTO = new DepartmentDTO(
                1L, "IT", null, "Tech dept", "123-456", "it@example.com", "Main st. 1"
        );

        // mock User и Department
        User user = mock(User.class);
        Department dept = mock(Department.class);

        when(user.toDto()).thenReturn(userDTO);
        when(dept.toDto()).thenReturn(deptDTO);

        PageResult<User> userPage = new PageResult<>(List.of(user), 1, 10, 1L);
        PageResult<Department> deptPage = new PageResult<>(List.of(dept), 1, 10, 1L);

        when(userService.getAllByRequest(eq(request), any())).thenReturn(userPage);
        when(departmentService.getAllByRequest(eq(request), any())).thenReturn(deptPage);

        // when
        SearchResponse response = searchController.search(request, pageParam);

        // then
        assertThat(response).isNotNull();
        assertThat(response.users()).containsExactly(userDTO);
        assertThat(response.departments()).containsExactly(deptDTO);
        assertThat(response.pageCount()).isEqualTo(1);
    }

    @Test
    void testSearch_PageMinusOne_ResetsToZero() {
        // given
        String request = "anna";
        PageParam pageParam = new PageParam();
        pageParam.setPage(-1);
        pageParam.setSize(5);

        UserDTO userDTO = new UserDTO(
                2L, "anna@example.com", "Anna", "Smith", null,
                "USER", "Anna", "9876543210", null, "ACTIVE", List.of()
        );

        DepartmentDTO deptDTO = new DepartmentDTO(
                2L, "HR", null, "HR dept", "555-666", "hr@example.com", "Main st. 2"
        );

        User user = mock(User.class);
        Department dept = mock(Department.class);

        when(user.toDto()).thenReturn(userDTO);
        when(dept.toDto()).thenReturn(deptDTO);

        PageResult<User> userPage = new PageResult<>(List.of(user), 1, 5, 1L);
        PageResult<Department> deptPage = new PageResult<>(List.of(dept), 1, 5, 1L);

        when(userService.getAllByRequest(eq(request), any())).thenReturn(userPage);
        when(departmentService.getAllByRequest(eq(request), any())).thenReturn(deptPage);

        // when
        SearchResponse response = searchController.search(request, pageParam);

        // then
        assertThat(pageParam.getPage()).isZero();
        assertThat(response.users()).containsExactly(userDTO);
        assertThat(response.departments()).containsExactly(deptDTO);
        assertThat(response.pageCount()).isEqualTo(1);
    }

    @Test
    void testSearch_NoResults() {
        // given
        String request = "zzz";
        PageParam pageParam = new PageParam();
        pageParam.setPage(0);
        pageParam.setSize(10);

        PageResult<User> userPage = new PageResult<>(List.of(), 0, 10, 0L);
        PageResult<Department> deptPage = new PageResult<>(List.of(), 0, 10, 0L);

        when(userService.getAllByRequest(eq(request), any())).thenReturn(userPage);
        when(departmentService.getAllByRequest(eq(request), any())).thenReturn(deptPage);

        // when
        SearchResponse response = searchController.search(request, pageParam);

        // then
        assertThat(response.users()).isEmpty();
        assertThat(response.departments()).isEmpty();
        assertThat(response.pageCount()).isZero();
    }
}
