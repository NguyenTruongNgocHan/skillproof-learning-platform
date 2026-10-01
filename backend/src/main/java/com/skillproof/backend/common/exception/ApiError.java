package com.skillproof.backend.common.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
        ) {

    public static ApiError of(
            HttpStatus status,
            String code,
            String message,
            String path
    ) {

        return new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                path,
                null
        );
    }

    public static ApiError validation(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {

        return new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                path,
                fieldErrors
        );
    }
}
