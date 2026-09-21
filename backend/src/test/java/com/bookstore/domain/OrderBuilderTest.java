package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBuilderTest {
    @Test
    void buildsOrderWithConfiguredAttributesAndItems() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        Order order = Order.builder()
                .forUser(userId)
                .createdAt(createdAt)
                .addItem(bookId, "The Pragmatic Programmer", 2, new BigDecimal("19.99"))
                .build();

        assertEquals(userId, order.getUserId());
        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(bookId, order.getItems().get(0).getBookId());
        assertEquals("The Pragmatic Programmer", order.getItems().get(0).getBookTitle());
        assertEquals(2, order.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), order.getItems().get(0).getUnitPrice());
    }
}
