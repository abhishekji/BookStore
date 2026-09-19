package com.bookstore.exception;

import java.time.Instant;

public record ApiError(Instant timestamp, String correlationId, int status, String code, String message) { }
