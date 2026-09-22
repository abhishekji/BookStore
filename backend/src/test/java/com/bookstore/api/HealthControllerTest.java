package com.bookstore.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {
    @Test
    void reportsServiceHealth() {
        assertEquals(Map.of("status", "UP", "service", "bookstore-backend"),
                new HealthController().health());
    }
}
