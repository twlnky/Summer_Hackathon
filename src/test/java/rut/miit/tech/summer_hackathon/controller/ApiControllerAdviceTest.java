package rut.miit.tech.summer_hackathon.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import rut.miit.tech.summer_hackathon.domain.dto.ErrorDTO;

import static org.assertj.core.api.Assertions.assertThat;

class ApiControllerAdviceTest {

    private ApiControllerAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new ApiControllerAdvice();
    }

    @Test
    @DisplayName("handleAnyException — должен вернуть статус 500 и сообщение об ошибке")
    void handleAnyException_shouldReturnErrorDTO_withStatus500() {
        Exception ex = new Exception("Internal server failure");

        ErrorDTO result = advice.handleAnyException(ex);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("500");
        assertThat(result.message()).isEqualTo("Internal server failure");
        assertThat(result.date()).isNotNull();
    }

    @Test
    @DisplayName("handleAuthException — должен вернуть статус 401 при AuthenticationException")
    void handleAuthException_shouldReturnErrorDTO_withStatus401() {
        AuthenticationException ex = new AuthenticationException("Invalid credentials") {};

        ErrorDTO result = advice.handleAuthException(ex);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("401");
        assertThat(result.message()).isEqualTo("Invalid credentials");
        assertThat(result.date()).isNotNull();
    }

    @Test
    @DisplayName("handleAccessDeniedException — должен вернуть статус 403 при AccessDeniedException")
    void handleAccessDeniedException_shouldReturnErrorDTO_withStatus403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden area");

        ErrorDTO result = advice.handleAccessDeniedException(ex);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("403");
        assertThat(result.message()).isEqualTo("Forbidden area");
        assertThat(result.date()).isNotNull();
    }
}
