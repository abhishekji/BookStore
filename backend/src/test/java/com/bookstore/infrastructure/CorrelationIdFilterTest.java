package com.bookstore.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservesProvidedCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "client-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals("client-id", request.getAttribute(CorrelationIdFilter.ATTRIBUTE));
        assertEquals("client-id", response.getHeader("X-Correlation-Id"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (request1, response1) -> {
            assertNotNull(request1.getAttribute(CorrelationIdFilter.ATTRIBUTE));
        });

        String responseId = response.getHeader("X-Correlation-Id");
        assertNotNull(responseId);
        assertFalse(responseId.isBlank());
    }
}
