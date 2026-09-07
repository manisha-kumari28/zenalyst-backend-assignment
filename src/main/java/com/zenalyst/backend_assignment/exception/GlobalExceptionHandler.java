package com.zenalyst.backend_assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        return Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                400,
                "error",
                "Bad Request",
                "message",
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericException(
            Exception exception) {

        return Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                500,
                "error",
                "Internal Server Error",
                "message",
                "An unexpected error occurred"
        );
    }
}