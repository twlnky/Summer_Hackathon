package rut.miit.tech.summer_hackathon.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rut.miit.tech.summer_hackathon.domain.dto.ErrorDTO;

import java.time.LocalDateTime;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({EntityNotFoundException.class, RuntimeException.class})
    public ErrorDTO handleNotFoundException(RuntimeException exception) {
        log.warn("Resource not found", exception);
        return ErrorDTO.builder()
                .status("404")
                .message(exception.getMessage())
                .date(LocalDateTime.now().toString())
                .build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ErrorDTO handleUnauthorized(AuthenticationException exception) {
        log.error("Unauthorized access", exception);
        return ErrorDTO.builder()
                .status("401")
                .message("Authentication failed: " + exception.getMessage())
                .date(LocalDateTime.now().toString())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorDTO handleAccessDenied(AccessDeniedException exception) {
        log.error("Access denied", exception);
        return ErrorDTO.builder()
                .status("403")
                .message("Access denied: " + exception.getMessage())
                .date(LocalDateTime.now().toString())
                .build();
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorDTO handleBadRequest(IllegalArgumentException exception) {
        log.error("Bad request", exception);
        return ErrorDTO.builder()
                .status("400")
                .message("Invalid request: " + exception.getMessage())
                .date(LocalDateTime.now().toString())
                .build();
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorDTO handleGeneralException(Exception exception) {
        log.error("Unexpected error occurred", exception);
        return ErrorDTO.builder()
                .status("500")
                .message("Internal server error")
                .date(LocalDateTime.now().toString())
                .build();
    }
}
