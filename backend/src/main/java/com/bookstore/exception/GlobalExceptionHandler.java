package com.bookstore.exception;

import com.bookstore.infrastructure.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(error(request, 400, "VALIDATION_ERROR", ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> internal(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(request, 500, "INTERNAL_ERROR", "An unexpected error occurred"));
    }
    private ApiError error(HttpServletRequest request, int status, String code, String message) {
        String correlationId = (String) request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        return new ApiError(Instant.now(), correlationId, status, code, message);
    }
}
