package com.bookstore.application.orders;

import com.bookstore.domain.Order;
import com.bookstore.dto.OrderDtos;
import com.bookstore.dto.OrderPageResponse;
import com.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderQueryServiceTest {
    private final OrderRepository orders = mock(OrderRepository.class);
    private final OrderQueryService service = new OrderQueryService(orders);
    private final UUID userId = UUID.randomUUID();

    @Test
    void returnsRequestedOrdersWithBoundedOffsetPagination() {
        Order order = new Order(userId);
        when(orders.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        OrderPageResponse result = service.findOrders(userId, -1, 99);

        assertEquals(0, result.offset());
        assertEquals(50, result.limit());
        assertEquals(1, result.content().size());
        verify(orders).findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class));
    }

    @Test
    void returnsAnOrderOnlyToItsOwner() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(userId);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        OrderDtos.OrderResponse result = service.findOrder(userId, orderId);

        assertEquals(Order.OrderStatus.PLACED, result.status());
    }

    @Test
    void rejectsAccessToAnotherUsersOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID());
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> service.findOrder(userId, orderId));
    }
}
