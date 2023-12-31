package com.example.banking.controller;

import com.example.banking.dto.ApiError;
import com.example.banking.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleInvalidFields(MethodArgumentNotValidException e) {
        log.error("[{}]: {}", BAD_REQUEST.value(), e.getMessage());
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(error -> error.getDefaultMessage() != null)
                .collect(toMap(FieldError::getField, FieldError::getDefaultMessage));
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({
            BadCredentialsException.class,
            UserNotFoundException.class,
            CardNotFoundException.class
    })
    public ApiError handleClientBadRequest(RuntimeException e, HttpServletRequest request) {
        log.error("[{}]: {}", BAD_REQUEST.value(), e.getMessage());
        return new ApiError(request.getRequestURI(), now(), BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler({
            CardsLimitException.class,
            ViolationPrivacyException.class,
            TransactionNotAvailableException.class
    })
    public ApiError handleClientForbiddenActions(RuntimeException e, HttpServletRequest request) {
        log.error("[{}]: {}", FORBIDDEN.value(), e.getMessage());
        return new ApiError(request.getRequestURI(), now(), FORBIDDEN.value(), e.getMessage());
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RoleNotFoundException.class, IllegalStateException.class})
    public ApiError handleServerError(RuntimeException e, HttpServletRequest request) {
        log.error("[{}]: {}", INTERNAL_SERVER_ERROR.value(), e.getMessage());
        return new ApiError(request.getRequestURI(), now(), INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

}