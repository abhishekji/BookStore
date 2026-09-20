package com.bookstore.exception;

import java.time.Instant;

public record ApiError(Instant timestamp, String correlationId, int status, String error,
                       String message, String path) {
    public String code() {
        return error;
    }
}
