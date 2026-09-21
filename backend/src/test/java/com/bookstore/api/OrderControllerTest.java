package com.bookstore.api;

import com.bookstore.application.checkout.CheckoutApplicationService;
import com.bookstore.application.checkout.CheckoutResult;
import com.bookstore.domain.UserAccount;
import com.bookstore.dto.OrderDtos;
import com.bookstore.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {
    private final CheckoutApplicationService service = mock(CheckoutApplicationService.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final OrderController controller = new OrderController(service, users);
    private final UUID userId = UUID.randomUUID();
    private final TestingAuthenticationToken authentication = new TestingAuthenticationToken("reader@example.com", "password");

    @Test
    void returnsCreatedForNewCheckoutAndOkForIdempotentReplay() {
        OrderDtos.OrderResponse order = order();
        authenticate();
        when(service.checkout(userId, "new-key")).thenReturn(new CheckoutResult(order, false));
        when(service.checkout(userId, "retry-key")).thenReturn(new CheckoutResult(order, true));

        assertEquals(HttpStatus.CREATED, controller.checkout(authentication, "new-key").getStatusCode());
        assertEquals(HttpStatus.OK, controller.checkout(authentication, "retry-key").getStatusCode());
        assertSame(order, controller.checkout(authentication, "new-key").getBody());
    }

    @Test
    void returnsOrderHistoryAndSingleOrderForCurrentUserOnly() {
        OrderDtos.OrderResponse order = order();
        authenticate();
        when(service.findOrders(userId)).thenReturn(List.of(order));
        when(service.findOrder(userId, order.id())).thenReturn(order);

        assertEquals(List.of(order), controller.orders(authentication));
        assertSame(order, controller.order(authentication, order.id()));
    }

    @Test
    void rejectsDeletedAuthenticatedAccount() {
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.orders(authentication));
    }

    private void authenticate() {
        UserAccount user = mock(UserAccount.class);
        when(user.getId()).thenReturn(userId);
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
    }

    private OrderDtos.OrderResponse order() {
        return new OrderDtos.OrderResponse(UUID.randomUUID(), List.of(), BigDecimal.ZERO,
                com.bookstore.domain.Order.OrderStatus.CONFIRMED, java.time.Instant.now());
    }
}
