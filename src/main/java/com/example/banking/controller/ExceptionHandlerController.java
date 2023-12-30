package com.example.banking.controller;

import com.example.banking.dto.ApiError;
import com.example.banking.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Map<String, String> handleInvalidFields(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(error -> error.getDefaultMessage() != null)
                .collect(toMap(FieldError::getField, FieldError::getDefaultMessage));
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({BadCredentialsException.class, UserNotFoundException.class})
    public ApiError handle(RuntimeException e, HttpServletRequest request) {
        return new ApiError(request.getRequestURI(), now(), BAD_REQUEST.value(), e.getMessage());
    }

}