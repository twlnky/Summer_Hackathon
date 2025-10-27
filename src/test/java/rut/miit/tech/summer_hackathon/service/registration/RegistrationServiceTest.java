package rut.miit.tech.summer_hackathon.service.registration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.util.DtoConverter;
import rut.miit.tech.summer_hackathon.repository.UserRepository;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DtoConverter dtoConverter;

    @InjectMocks
    private RegistrationService registrationService;

    private RegisterDTO createTestRegisterDTO() {
        return new RegisterDTO(
                "testuser",
                "password123",
                "password123",
                "test@example.com"
        );
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .firstName("testuser") // username маппится в firstName
                .email("test@example.com")
                .departments(new ArrayList<>())
                .moderator(null)
                .build();
    }

    @Test
    void register_ShouldConvertDtoAndSaveUser() {

        RegisterDTO registerDTO = createTestRegisterDTO();
        User expectedUser = createTestUser();

        when(dtoConverter.toModel(registerDTO, User.class)).thenReturn(expectedUser);
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);

        User result = registrationService.register(registerDTO);

        assertNotNull(result);
        assertEquals(expectedUser, result);

        assertEquals("testuser", result.getFirstName());
        assertEquals("test@example.com", result.getEmail());

        verify(dtoConverter).toModel(registerDTO, User.class);
        verify(userRepository).save(expectedUser);
    }

    @Test
    void register_ShouldReturnSavedUserWithId() {

        RegisterDTO registerDTO = createTestRegisterDTO();

        User userToSave = User.builder()
                .firstName("testuser")
                .email("test@example.com")
                .departments(new ArrayList<>())
                .build();

        User savedUser = User.builder()
                .id(123L)
                .firstName("testuser")
                .email("test@example.com")
                .departments(new ArrayList<>())
                .build();

        when(dtoConverter.toModel(registerDTO, User.class)).thenReturn(userToSave);
        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = registrationService.register(registerDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getFirstName());
        assertEquals("test@example.com", result.getEmail());

        verify(dtoConverter).toModel(registerDTO, User.class);
        verify(userRepository).save(userToSave);
    }

    @Test
    void register_ShouldHandleDifferentUsernameAndEmail() {

        RegisterDTO registerDTO = new RegisterDTO(
                "differentuser",
                "mypassword",
                "mypassword",
                "different@test.com"
        );

        User userToSave = User.builder()
                .firstName("differentuser")
                .email("different@test.com")
                .departments(new ArrayList<>())
                .build();

        User savedUser = User.builder()
                .id(1L)
                .firstName("differentuser")
                .email("different@test.com")
                .departments(new ArrayList<>())
                .build();

        when(dtoConverter.toModel(registerDTO, User.class)).thenReturn(userToSave);
        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = registrationService.register(registerDTO);

        assertNotNull(result);
        assertEquals("differentuser", result.getFirstName());
        assertEquals("different@test.com", result.getEmail());
        verify(dtoConverter).toModel(registerDTO, User.class);
        verify(userRepository).save(userToSave);
    }

    @Test
    void register_ShouldWorkWithMinimalValidData() {

        RegisterDTO minimalDTO = new RegisterDTO(
                "minimal",
                "pass",
                "pass",
                "minimal@test.com"
        );

        User userToSave = User.builder()
                .firstName("minimal")
                .email("minimal@test.com")
                .departments(new ArrayList<>())
                .build();

        User savedUser = User.builder()
                .id(1L)
                .firstName("minimal")
                .email("minimal@test.com")
                .departments(new ArrayList<>())
                .build();

        when(dtoConverter.toModel(minimalDTO, User.class)).thenReturn(userToSave);
        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = registrationService.register(minimalDTO);

        assertNotNull(result);
        assertEquals("minimal", result.getFirstName());
        assertEquals("minimal@test.com", result.getEmail());

        assertNull(result.getLastName());
        assertNull(result.getMiddleName());
        assertNull(result.getPersonalPhone());
        assertNull(result.getBusinessPhone());
        assertNull(result.getPosition());
        assertNull(result.getNote());
        assertNull(result.getOfficeNumber());
        assertNull(result.getModerator());
        assertNotNull(result.getDepartments());
        assertTrue(result.getDepartments().isEmpty());

        verify(dtoConverter).toModel(minimalDTO, User.class);
        verify(userRepository).save(userToSave);
    }

    @Test
    void register_ShouldCallConverterAndRepositoryExactlyOnce() {

        RegisterDTO registerDTO = createTestRegisterDTO();
        User userToSave = createTestUser();
        userToSave.setId(null); // Убедимся, что ID не установлен до сохранения

        User savedUser = createTestUser(); // После сохранения имеет ID

        when(dtoConverter.toModel(registerDTO, User.class)).thenReturn(userToSave);
        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = registrationService.register(registerDTO);

        assertNotNull(result);

        verify(dtoConverter, times(1)).toModel(registerDTO, User.class);
        verify(userRepository, times(1)).save(userToSave);

        verifyNoMoreInteractions(dtoConverter, userRepository);
    }

    @Test
    void register_ShouldHandleUserCreationWithoutIdBeforeSave() {

        RegisterDTO registerDTO = createTestRegisterDTO();

        User userWithoutId = User.builder()
                .firstName("testuser")
                .email("test@example.com")
                .departments(new ArrayList<>())
                .build();

        User userWithId = User.builder()
                .id(456L)
                .firstName("testuser")
                .email("test@example.com")
                .departments(new ArrayList<>())
                .build();

        when(dtoConverter.toModel(registerDTO, User.class)).thenReturn(userWithoutId);
        when(userRepository.save(userWithoutId)).thenReturn(userWithId);

        User result = registrationService.register(registerDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getFirstName());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).save(argThat(user -> user.getId() == null));
    }
}