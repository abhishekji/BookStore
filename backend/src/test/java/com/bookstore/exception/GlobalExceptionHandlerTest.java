package com.bookstore.exception;

import com.bookstore.infrastructure.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    @Test
    void mapsValidationExceptionsToContract() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(CorrelationIdFilter.ATTRIBUTE)).thenReturn("corr-1");

        var response = new GlobalExceptionHandler()
                .badRequest(new IllegalArgumentException("invalid book"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("corr-1", response.getBody().correlationId());
        assertEquals(400, response.getBody().status());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals("invalid book", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(CorrelationIdFilter.ATTRIBUTE)).thenReturn("corr-2");

        var response = new GlobalExceptionHandler().internal(new RuntimeException("secret"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void mapsIdempotencyConflictToConflictResponse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(CorrelationIdFilter.ATTRIBUTE)).thenReturn("corr-3");

        var response = new GlobalExceptionHandler().idempotencyConflict(
                new IdempotencyConflictException("key conflict"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IDEMPOTENCY_CONFLICT", response.getBody().code());
    }
}
