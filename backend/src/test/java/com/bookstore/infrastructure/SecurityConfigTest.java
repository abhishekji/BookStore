package com.bookstore.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
    @Test
    void configuresReactCorsPolicy() {
        var source = new SecurityConfig(new ObjectMapper(), new CorsProperties(
                java.util.List.of("http://localhost:5173"),
                java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                java.util.List.of("*"),
                true), null).corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/books"));

        assertNotNull(configuration);
        assertEquals(java.util.List.of("http://localhost:5173"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowedMethods().contains("GET"));
        assertEquals(java.util.List.of("*"), configuration.getAllowedHeaders());
        assertTrue(configuration.getAllowCredentials());
    }
}
