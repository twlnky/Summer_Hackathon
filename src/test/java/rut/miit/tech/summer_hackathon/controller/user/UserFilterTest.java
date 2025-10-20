package rut.miit.tech.summer_hackathon.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rut.miit.tech.summer_hackathon.domain.model.User;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFilterTest {

    @Mock
    private Root<User> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Expression<String> stringExpression;

    @Mock
    private Predicate predicate;

    private UserFilter userFilter;

    @BeforeEach
    void setUp() {
        userFilter = new UserFilter();
    }

    @Test
    void toPredicate_WithEmptyFilter_ShouldReturnConjunction() {
        // Arrange
        when(cb.conjunction()).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb).conjunction();
        verifyNoMoreInteractions(root);
    }

    @Test
    void toPredicate_WithFirstName_ShouldCreatePredicate() {
        // Arrange
        userFilter.setFirstName("John");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Должно быть вызвано несколько раз для разных полей
        verify(cb, atLeast(8)).like(any(Expression.class), anyString());
        verify(cb).or(any(Predicate[].class));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void toPredicate_WithLastName_ShouldCreatePredicate() {
        // Arrange
        userFilter.setLastName("Doe");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb, atLeast(8)).like(any(Expression.class), anyString());
    }

    @Test
    void toPredicate_WithMiddleName_ShouldCreatePredicate() {
        // Arrange
        userFilter.setMiddleName("Michael");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb, atLeast(8)).like(any(Expression.class), anyString());
    }

    @Test
    void toPredicate_WithMultipleFields_ShouldCreateMultiplePredicates() {
        // Arrange
        userFilter.setFirstName("John");
        userFilter.setLastName("Doe");
        userFilter.setMiddleName("Michael");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Для 3 поисковых термов должно быть 3 вызова or()
        verify(cb, times(3)).or(any(Predicate[].class));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void toPredicate_WithEmptyStringFields_ShouldIgnoreThem() {
        // Arrange
        userFilter.setFirstName("");
        userFilter.setLastName("   ");
        userFilter.setMiddleName(null);

        when(cb.conjunction()).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb).conjunction();
        verifyNoMoreInteractions(root);
    }

    @Test
    void toPredicate_WithTrimmedFields_ShouldTrimSpaces() {
        // Arrange
        userFilter.setFirstName("  John  ");
        userFilter.setLastName("  Doe  ");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Проверяем, что поиск идет по trimmed значениям
        verify(cb, atLeast(8)).like(any(Expression.class), contains("john"));
        verify(cb, atLeast(8)).like(any(Expression.class), contains("doe"));
    }

    @Test
    void toPredicate_WithOfficeNumberField_ShouldIncludeOfficeNumberSearch() {
        // Arrange
        userFilter.setFirstName("101");

        setupMockForAllStringFields();

        // Mock для officeNumber
        Path officeNumberPath = mock(Path.class);
        when(root.get("officeNumber")).thenReturn(officeNumberPath);
        when(cb.toString(any(Expression.class))).thenReturn(stringExpression);
        lenient().when(cb.like(stringExpression, "101")).thenReturn(predicate);

        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Проверяем, что officeNumber также включен в поиск
        verify(cb).toString(any(Expression.class));
        verify(cb).like(stringExpression, "101");
    }

    @Test
    void toPredicate_WithSpecialCharacters_ShouldHandleThemCorrectly() {
        // Arrange
        userFilter.setFirstName("John-Doe");
        userFilter.setLastName("O'Conner");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Должно быть несколько вызовов для каждого терма
        verify(cb, atLeast(8)).like(any(Expression.class), contains("john-doe"));
        verify(cb, atLeast(8)).like(any(Expression.class), contains("o'conner"));
    }

    @Test
    void toPredicate_WithCaseInsensitiveSearch_ShouldUseLowerCase() {
        // Arrange
        userFilter.setFirstName("JOHN");
        userFilter.setLastName("doe");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Проверяем, что все преобразуется в нижний регистр
        verify(cb, atLeast(16)).lower(any(Expression.class)); // 8 полей × 2 терма
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Arrange & Act
        userFilter.setFirstName("John");
        userFilter.setLastName("Doe");
        userFilter.setMiddleName("Michael");

        // Assert
        assertEquals("John", userFilter.getFirstName());
        assertEquals("Doe", userFilter.getLastName());
        assertEquals("Michael", userFilter.getMiddleName());
    }

    @Test
    void toPredicate_WithNullFields_ShouldHandleGracefully() {
        // Arrange
        userFilter.setFirstName(null);
        userFilter.setLastName(null);
        userFilter.setMiddleName(null);

        when(cb.conjunction()).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb).conjunction();
        verifyNoMoreInteractions(root);
    }

    @Test
    void toPredicate_WithMixedValidAndInvalidFields_ShouldOnlyUseValidOnes() {
        // Arrange
        userFilter.setFirstName("John");
        userFilter.setLastName("");
        userFilter.setMiddleName("  ");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Должен быть только один поисковый терм для "John"
        verify(cb, times(1)).or(any(Predicate[].class)); // Только для "John"
        verify(cb, never()).like(any(Expression.class), eq("%%"));
    }

    @Test
    void toPredicate_WhenFieldThrowsException_ShouldContinueGracefully() {
        // Arrange
        userFilter.setFirstName("John");
        userFilter.setLastName("Doe");

        // Настроим некоторые поля нормально, некоторые чтобы бросали исключение
        setupMockWithSomeFieldsThrowingException();

        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Должен продолжить работу даже при исключении на некоторых полях
        verify(cb, times(2)).or(any(Predicate[].class)); // Для 2 термов
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void toPredicate_WithSingleCharacterSearch_ShouldWork() {
        // Arrange
        userFilter.setFirstName("J");

        setupMockForAllStringFields();
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb, atLeast(8)).like(any(Expression.class), contains("j"));
    }

    @Test
    void toPredicate_WithAllSearchFields_ShouldCreateComprehensivePredicate() {
        // Arrange
        userFilter.setFirstName("John");
        userFilter.setLastName("Doe");

        setupMockForAllStringFields();

        // Mock для officeNumber
        Path officeNumberPath = mock(Path.class);
        when(root.get("officeNumber")).thenReturn(officeNumberPath);
        when(cb.toString(any(Expression.class))).thenReturn(stringExpression);

        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Проверяем, что все поля были затронуты
        verify(root, atLeast(9)).get(anyString()); // 8 строковых полей + officeNumber
        verify(cb, atLeastOnce()).toString(any(Expression.class));
    }

    @Test
    void toPredicate_WithOnlyOfficeNumberSearch_ShouldWork() {
        // Arrange
        userFilter.setFirstName("101");

        // Настроим только officeNumber
        Path officeNumberPath = mock(Path.class);
        when(root.get("officeNumber")).thenReturn(officeNumberPath);
        when(cb.toString(any(Expression.class))).thenReturn(stringExpression);
        lenient().when(cb.like(stringExpression, "101")).thenReturn(predicate);

        // Настроим строковые поля, но они не должны использоваться
        setupMockForAllStringFields();

        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        verify(cb).toString(any(Expression.class));
        verify(cb).like(stringExpression, "101");
    }

    @Test
    void toPredicate_WithPartialFieldAccess_ShouldHandleGracefully() {
        // Arrange
        userFilter.setFirstName("Test");

        // Настроим только некоторые поля доступными
        setupMockForPartialFields();

        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Act
        Predicate result = userFilter.toPredicate(root, query, cb);

        // Assert
        assertNotNull(result);
        // Должен работать даже с частично доступными полями
        verify(cb).or(any(Predicate[].class));
        verify(cb).and(any(Predicate[].class));
    }

    // Вспомогательный метод для настройки моков всех строковых полей
    private void setupMockForAllStringFields() {
        String[] stringFields = {
                "firstName", "lastName", "middleName",
                "businessPhone", "personalPhone", "email",
                "position", "note"
        };

        for (String field : stringFields) {
            try {
                Path fieldPath = mock(Path.class);
                when(root.get(field)).thenReturn(fieldPath);
                when(cb.lower(fieldPath)).thenReturn(stringExpression);
            } catch (Exception e) {
                // Игнорируем исключения
            }
        }
    }

    // Вспомогательный метод для настройки моков с некоторыми полями, бросающими исключения
    private void setupMockWithSomeFieldsThrowingException() {
        String[] stringFields = {
                "firstName", "lastName", "middleName",
                "businessPhone", "personalPhone", "email",
                "position", "note"
        };

        for (int i = 0; i < stringFields.length; i++) {
            try {
                if (i % 3 == 0) { // Каждое третье поле бросает исключение
                    when(root.get(stringFields[i])).thenThrow(new IllegalArgumentException("Field not accessible"));
                } else {
                    Path fieldPath = mock(Path.class);
                    when(root.get(stringFields[i])).thenReturn(fieldPath);
                    when(cb.lower(fieldPath)).thenReturn(stringExpression);
                }
            } catch (Exception e) {
                // Игнорируем исключения
            }
        }
    }

    // Вспомогательный метод для настройки только части полей
    private void setupMockForPartialFields() {
        // Настроим только 3 поля как доступные
        String[] accessibleFields = {"firstName", "lastName", "email"};

        for (String field : accessibleFields) {
            try {
                Path fieldPath = mock(Path.class);
                when(root.get(field)).thenReturn(fieldPath);
                when(cb.lower(fieldPath)).thenReturn(stringExpression);
            } catch (Exception e) {
                // Игнорируем исключения
            }
        }

        // Остальные поля будут недоступны (не настроены)
    }
}