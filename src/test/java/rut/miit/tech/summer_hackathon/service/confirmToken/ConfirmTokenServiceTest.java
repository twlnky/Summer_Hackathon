package rut.miit.tech.summer_hackathon.service.confirmToken;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rut.miit.tech.summer_hackathon.domain.model.ConfirmToken;
import rut.miit.tech.summer_hackathon.repository.ConfirmTokenRepository;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmTokenServiceTest {

    @Mock
    private ConfirmTokenRepository confirmTokenRepository;

    @InjectMocks
    private ConfirmTokenService confirmTokenService;

    @Test
    void confirmToken_ValidToken_CallsRepositorySave() {
        ConfirmToken confirmToken = createTestConfirmToken(123456, 1L);

        confirmTokenService.confirmToken(confirmToken);

        verify(confirmTokenRepository, times(1)).save(confirmToken);
    }

    @Test
    void confirmToken_NullToken_CallsRepositorySaveWithNull() {
        ConfirmToken nullToken = null;

        confirmTokenService.confirmToken(nullToken);

        verify(confirmTokenRepository, times(1)).save(nullToken);
    }

    @Test
    void confirmToken_MultipleCalls_CallsRepositorySaveMultipleTimes() {
        ConfirmToken token1 = createTestConfirmToken(1234, 1L);
        ConfirmToken token2 = createTestConfirmToken(5678, 2L);

        confirmTokenService.confirmToken(token1);
        confirmTokenService.confirmToken(token2);

        verify(confirmTokenRepository, times(2)).save(any(ConfirmToken.class));
        verify(confirmTokenRepository, times(1)).save(token1);
        verify(confirmTokenRepository, times(1)).save(token2);
    }

    @Test
    void confirmToken_NoOtherRepositoryMethodsCalled_OnlySaveIsCalled() {
        // Arrange
        ConfirmToken confirmToken = createTestConfirmToken(123456, 1L);

        confirmTokenService.confirmToken(confirmToken);

        verify(confirmTokenRepository, only()).save(confirmToken);
    }

    @Test
    void confirmToken_NewTokenInstance_CallsRepositorySave() {
        ConfirmToken confirmToken = new ConfirmToken();
        ReflectionTestUtils.setField(confirmToken, "token", 999999);

        confirmTokenService.confirmToken(confirmToken);

        verify(confirmTokenRepository, times(1)).save(confirmToken);
    }

    @Test
    void confirmToken_TokenWithZeroValue_CallsRepositorySave() {
        ConfirmToken confirmToken = createTestConfirmToken(0, 1L);

        confirmTokenService.confirmToken(confirmToken);

        verify(confirmTokenRepository, times(1)).save(confirmToken);
    }

    private ConfirmToken createTestConfirmToken(int tokenValue, Long id) {
        ConfirmToken token = new ConfirmToken();
        ReflectionTestUtils.setField(token, "id", id);
        ReflectionTestUtils.setField(token, "token", tokenValue);
        return token;
    }
}