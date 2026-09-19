package com.bookstore.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
    @Test
    void configuresReactCorsPolicy() {
        var source = new SecurityConfig().corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/books"));

        assertNotNull(configuration);
        assertEquals(java.util.List.of("http://localhost:5173"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowedMethods().contains("GET"));
        assertEquals(java.util.List.of("*"), configuration.getAllowedHeaders());
        assertTrue(configuration.getAllowCredentials());
    }
}
