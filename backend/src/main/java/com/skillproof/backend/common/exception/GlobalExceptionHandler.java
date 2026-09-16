package com.skillproof.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiError>
    handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ApiError error =
                ApiError.validation(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR",
                        "The request contains invalid fields.",
                        request.getRequestURI(),
                        fieldErrors
                );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<ApiError>
    handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {

        ApiError error =
                ApiError.of(
                        HttpStatus.BAD_REQUEST,
                        "CONSTRAINT_VIOLATION",
                        "The request violates a constraint.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(
            BadRequestException.class
    )
    public ResponseEntity<ApiError>
    handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {

        ApiError error =
                ApiError.of(
                        HttpStatus.BAD_REQUEST,
                        exception.getCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(
            ConflictException.class
    )
    public ResponseEntity<ApiError>
    handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {

        ApiError error =
                ApiError.of(
                        HttpStatus.CONFLICT,
                        exception.getCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError>
    handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {

        log.error(
                "Unhandled exception for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        ApiError error =
                ApiError.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR",
                        "An unexpected error occurred.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(error);
    }
}