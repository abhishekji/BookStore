package com.bookstore.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiSecurityErrorHandlerTest {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void returnsStructuredUnauthorizedResponse() throws Exception {
        MockHttpServletRequest request = requestWithCorrelationId();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ApiAuthenticationEntryPoint(objectMapper)
                .commence(request, response, new BadCredentialsException("invalid"));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("\"error\":\"AUTHENTICATION_REQUIRED\""));
        assertEquals("Basic realm=\"bookstore\"", response.getHeader("WWW-Authenticate"));
    }

    @Test
    void returnsStructuredForbiddenResponse() throws Exception {
        MockHttpServletRequest request = requestWithCorrelationId();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ApiAccessDeniedHandler(objectMapper)
                .handle(request, response, new AccessDeniedException("denied"));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("\"error\":\"ACCESS_DENIED\""));
    }

    private MockHttpServletRequest requestWithCorrelationId() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE, "correlation-id");
        return request;
    }
}
