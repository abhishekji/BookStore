package com.bookstore.infrastructure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InfrastructureComponentsTest {
    private static final String SECRET = "a-secret-that-is-longer-than-thirty-two-characters";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtServiceSignsTokensAndRejectsInvalidSecretsAndTokens() {
        JwtService service = new JwtService(SECRET, Duration.ofMinutes(5));

        assertEquals("reader@example.com", service.subject(service.generate("reader@example.com")));
        assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short", Duration.ofMinutes(5)));
        assertThrows(JwtException.class, () -> service.subject("not-a-jwt"));
    }

    @Test
    void jwtFilterAuthenticatesValidBearerTokensAndAlwaysContinuesChain() throws Exception {
        JwtService jwt = new JwtService(SECRET, Duration.ofMinutes(5));
        UserDetailsService users = mock(UserDetailsService.class);
        when(users.loadUserByUsername("reader@example.com"))
                .thenReturn(User.withUsername("reader@example.com").password("unused").authorities("ROLE_USER").build());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt.generate("reader@example.com"));
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals("reader@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(chain).doFilter(eq(request), any());
    }

    @Test
    void jwtFilterIgnoresMissingInvalidAndAlreadyAuthenticatedBearerTokens() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UserDetailsService users = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users);
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
        verifyNoInteractions(jwt, users);

        MockHttpServletRequest invalid = new MockHttpServletRequest();
        invalid.addHeader("Authorization", "Bearer invalid");
        when(jwt.subject("invalid")).thenThrow(new JwtException("bad token"));
        filter.doFilter(invalid, new MockHttpServletResponse(), chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.TestingAuthenticationToken("existing", "password"));
        MockHttpServletRequest existing = new MockHttpServletRequest();
        existing.addHeader("Authorization", "Bearer ignored");
        filter.doFilter(existing, new MockHttpServletResponse(), chain);
        verify(jwt).subject("invalid");
        verifyNoMoreInteractions(jwt);
    }

    @Test
    void offsetPageRequestNavigatesOffsetsWithoutLosingSorting() {
        Sort sort = Sort.by("title").ascending();
        OffsetBasedPageRequest page = new OffsetBasedPageRequest(10, 5, sort);

        assertEquals(2, page.getPageNumber());
        assertEquals(5, page.getPageSize());
        assertEquals(10, page.getOffset());
        assertEquals(sort, page.getSort());
        assertTrue(page.hasPrevious());
        assertEquals(15, page.next().getOffset());
        assertEquals(5, page.previousOrFirst().getOffset());
        assertEquals(0, page.first().getOffset());
        assertEquals(15, page.withPage(3).getOffset());
        assertFalse(new OffsetBasedPageRequest(0, 5, sort).hasPrevious());
        assertEquals(0, new OffsetBasedPageRequest(0, 5, sort).previousOrFirst().getOffset());
    }

    @Test
    void openApiDescribesBothSupportedAuthenticationSchemes() {
        var api = new OpenApiConfig().bookstoreOpenAPI();

        assertEquals("Simple Online Bookstore API", api.getInfo().getTitle());
        assertEquals("bearer", api.getComponents().getSecuritySchemes().get("bearerAuth").getScheme());
        assertEquals("basic", api.getComponents().getSecuritySchemes().get("basicAuth").getScheme());
    }

    @Test
    void structuredLoggerEmitsBusinessEventsWithCorrelationId() {
        Logger logger = (Logger) LoggerFactory.getLogger(StructuredBusinessEventLogger.class);
        ListAppender<ILoggingEvent> events = new ListAppender<>();
        events.start();
        logger.addAppender(events);
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        org.slf4j.MDC.put("correlationId", "correlation-1");
        try {
            StructuredBusinessEventLogger businessEvents = new StructuredBusinessEventLogger();
            businessEvents.cartItemAdded(userId, bookId, 2, 3);
            businessEvents.cartItemRemoved(userId, bookId, false);
            businessEvents.checkoutStarted(userId);
            businessEvents.checkoutCompleted(userId, UUID.randomUUID());
            businessEvents.checkoutReplayed(userId, UUID.randomUUID());
            businessEvents.checkoutRejected(userId, "insufficient_stock");

            assertEquals(6, events.list.size());
            assertTrue(events.list.stream().anyMatch(event -> event.getFormattedMessage().contains("cart_item_added")));
            assertTrue(events.list.stream().anyMatch(event -> event.getFormattedMessage().contains("outcome=not_found")));
            assertTrue(events.list.stream().anyMatch(event -> event.getFormattedMessage().contains("correlationId=correlation-1")));
        } finally {
            org.slf4j.MDC.clear();
            logger.detachAppender(events);
        }
    }
}
