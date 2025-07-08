package rut.miit.tech.summer_hackathon.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rut.miit.tech.summer_hackathon.domain.dto.ErrorDTO;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {


    @ResponseStatus
    @ExceptionHandler
    public ErrorDTO handleAnyException(Exception exception) {
        log.error("Unexpected error", exception);
        return ErrorDTO.builder()
                .date(LocalDateTime.now().toString())
                .message(exception.getMessage())
                .status("500")
                .build();
    }


    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ErrorDTO handleAuthException(AuthenticationException exception) {
        log.error("Authentication error", exception);
        return ErrorDTO.builder()
                .date(LocalDateTime.now().toString())
                .message(exception.getMessage())
                .status("401")
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorDTO handleAccessDeniedException(AccessDeniedException exception) {
        log.error("Access Denied", exception);
        return ErrorDTO.builder()
                .date(LocalDateTime.now().toString())
                .message(exception.getMessage())
                .status("403")
                .build();
    }
}