package rut.miit.tech.summer_hackathon.controller.department;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rut.miit.tech.summer_hackathon.domain.model.Department;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DepartmentFilterTest {

    private DepartmentFilter departmentFilter;

    @Mock
    private Root<Department> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @BeforeEach
    void setUp() {
        departmentFilter = new DepartmentFilter();
    }

    @Test
    void toPredicate_WithNullName_ShouldReturnNull() {
        // Arrange
        departmentFilter.setName(null);

        // Act
        Predicate result = departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertNull(result);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void toPredicate_WithEmptyName_ShouldReturnNull() {
        // Arrange
        departmentFilter.setName("   ");

        // Act
        Predicate result = departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertNull(result);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void toPredicate_WithValidName_ShouldCreatePredicates() {
        // Arrange
        String searchName = "IT Department";
        departmentFilter.setName(searchName);
        String expectedSearchTerm = searchName.trim().toLowerCase();

        // Создаем реальные моки для Path
        Path namePath = mock(Path.class);
        Path tagsPath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate tagPredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("tags")).thenReturn(tagsPath);
        when(criteriaBuilder.lower(namePath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(lowerExpression, "%" + expectedSearchTerm + "%"))
                .thenReturn(namePredicate);
        when(criteriaBuilder.isMember(expectedSearchTerm, tagsPath))
                .thenReturn(tagPredicate);
        when(criteriaBuilder.or(namePredicate, tagPredicate)).thenReturn(orPredicate);

        // Act
        Predicate result = departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertNotNull(result);
        assertEquals(orPredicate, result);

        // Проверяем цепочку вызовов
        verify(root).get("name");
        verify(root).get("tags");
        verify(criteriaBuilder).lower(namePath);
        verify(criteriaBuilder).like(lowerExpression, "%" + expectedSearchTerm + "%");
        verify(criteriaBuilder).isMember(expectedSearchTerm, tagsPath);
        verify(criteriaBuilder).or(namePredicate, tagPredicate);
    }

    @Test
    void toPredicate_WithNameContainingSpaces_ShouldTrimAndConvertToLowerCase() {
        // Arrange
        String searchName = "  IT Department  ";
        departmentFilter.setName(searchName);
        String expectedSearchTerm = "it department";

        Path namePath = mock(Path.class);
        Path tagsPath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate tagPredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("tags")).thenReturn(tagsPath);
        when(criteriaBuilder.lower(namePath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(lowerExpression, "%" + expectedSearchTerm + "%"))
                .thenReturn(namePredicate);
        when(criteriaBuilder.isMember(expectedSearchTerm, tagsPath))
                .thenReturn(tagPredicate);
        when(criteriaBuilder.or(namePredicate, tagPredicate)).thenReturn(orPredicate);

        // Act
        Predicate result = departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertNotNull(result);
        verify(criteriaBuilder).like(lowerExpression, "%" + expectedSearchTerm + "%");
        verify(criteriaBuilder).isMember(expectedSearchTerm, tagsPath);
    }

    @Test
    void toPredicate_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Arrange
        String searchName = "IT-Department@2024";
        departmentFilter.setName(searchName);
        String expectedSearchTerm = "it-department@2024";

        Path namePath = mock(Path.class);
        Path tagsPath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate tagPredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("tags")).thenReturn(tagsPath);
        when(criteriaBuilder.lower(namePath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(lowerExpression, "%" + expectedSearchTerm + "%"))
                .thenReturn(namePredicate);
        when(criteriaBuilder.isMember(expectedSearchTerm, tagsPath))
                .thenReturn(tagPredicate);
        when(criteriaBuilder.or(namePredicate, tagPredicate)).thenReturn(orPredicate);

        // Act
        Predicate result = departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertNotNull(result);
        verify(criteriaBuilder).like(lowerExpression, "%" + expectedSearchTerm + "%");
        verify(criteriaBuilder).isMember(expectedSearchTerm, tagsPath);
    }

    @Test
    void toPredicate_ShouldCallLowerForName() {
        // Arrange
        String searchName = "Test";
        departmentFilter.setName(searchName);

        Path namePath = mock(Path.class);
        Path tagsPath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("tags")).thenReturn(tagsPath);
        when(criteriaBuilder.lower(namePath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.isMember(anyString(), any())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));

        // Act
        departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        verify(criteriaBuilder).lower(namePath);
    }

    @Test
    void toPredicate_ShouldUseOrCombination() {
        // Arrange
        String searchName = "Test";
        departmentFilter.setName(searchName);

        Path namePath = mock(Path.class);
        Path tagsPath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate tagPredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("tags")).thenReturn(tagsPath);
        when(criteriaBuilder.lower(namePath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(namePredicate);
        when(criteriaBuilder.isMember(anyString(), any())).thenReturn(tagPredicate);
        when(criteriaBuilder.or(namePredicate, tagPredicate)).thenReturn(orPredicate);

        // Act
        Predicate result = departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertNotNull(result);
        assertEquals(orPredicate, result);
        verify(criteriaBuilder).or(namePredicate, tagPredicate);
    }

    @Test
    void getName_ShouldReturnCorrectName() {
        // Arrange
        String expectedName = "Test Department";
        departmentFilter.setName(expectedName);

        // Act
        String actualName = departmentFilter.getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    void setName_ShouldSetNameCorrectly() {
        // Arrange
        String expectedName = "New Department Name";

        // Act
        departmentFilter.setName(expectedName);

        // Assert
        assertEquals(expectedName, departmentFilter.getName());
    }

    @Test
    void setName_WithNull_ShouldSetNull() {
        // Act
        departmentFilter.setName(null);

        // Assert
        assertNull(departmentFilter.getName());
    }

    @Test
    void toPredicate_ShouldCreateBothNameAndTagPredicates() {
        // Arrange
        String searchName = "Development";
        departmentFilter.setName(searchName);

        Path namePath = mock(Path.class);
        Path tagsPath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("tags")).thenReturn(tagsPath);
        when(criteriaBuilder.lower(namePath)).thenReturn(lowerExpression);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.isMember(anyString(), any())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));

        // Act
        departmentFilter.toPredicate(root, query, criteriaBuilder);

        // Assert
        verify(criteriaBuilder, times(1)).like(any(Expression.class), anyString());
        verify(criteriaBuilder, times(1)).isMember(anyString(), any());
        verify(criteriaBuilder, times(1)).or(any(Predicate.class), any(Predicate.class));
    }
}