package com.codibly.energymix.controller;

import com.codibly.energymix.service.InsufficientForecastDataException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates exceptions into small, consistent JSON error bodies.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(int status, String message) {
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request: the 'hours' parameter must be an integer between 1 and 6.");
    }

    @ExceptionHandler(InsufficientForecastDataException.class)
    public ResponseEntity<ApiError> handleInsufficientData(InsufficientForecastDataException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiError> handleUpstream(RestClientException ex) {
        return build(HttpStatus.BAD_GATEWAY, "Failed to reach the Carbon Intensity API. Please try again later.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message));
    }
}
