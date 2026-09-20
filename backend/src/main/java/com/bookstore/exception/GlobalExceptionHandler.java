package com.bookstore.exception;

import com.bookstore.infrastructure.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.core.AuthenticationException;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(error(request, HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidArguments(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(error(request, HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", message));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(error(request, HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST", "The request body is invalid or malformed"));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(error(request, HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", "The request contains invalid values"));
    }
    @ExceptionHandler({IdempotencyConflictException.class, CheckoutInProgressException.class})
    ResponseEntity<ApiError> idempotencyConflict(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(request, HttpStatus.CONFLICT.value(),
                        "IDEMPOTENCY_CONFLICT", ex.getMessage()));
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    ResponseEntity<ApiError> userConflict(UserAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(request, HttpStatus.CONFLICT.value(), "USER_ALREADY_EXISTS", ex.getMessage()));
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> authenticationFailure(AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error(request, HttpStatus.UNAUTHORIZED.value(),
                        "AUTHENTICATION_FAILED", "Invalid email or password"));
    }
    @ExceptionHandler({CartNotFoundException.class, CartItemNotFoundException.class})
    ResponseEntity<ApiError> cartNotFound(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(request, HttpStatus.NOT_FOUND.value(), "CART_ITEM_NOT_FOUND", ex.getMessage()));
    }
    @ExceptionHandler(BookNotFoundException.class)
    ResponseEntity<ApiError> notFound(BookNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(request, HttpStatus.NOT_FOUND.value(), "BOOK_NOT_FOUND", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> invalidRequest(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(error(request, HttpStatus.BAD_REQUEST.value(), "INVALID_REQUEST",
                        "The request contains an invalid value"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> internal(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(request, HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                        "An unexpected error occurred"));
    }
    private ApiError error(HttpServletRequest request, int status, String code, String message) {
        String correlationId = (String) request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        return new ApiError(Instant.now(), correlationId, status, code, message, request.getRequestURI());
    }
}
