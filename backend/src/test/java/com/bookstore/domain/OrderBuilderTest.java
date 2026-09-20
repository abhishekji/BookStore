package com.bookstore.domain;

import org.junit.jupiter.api.Test;

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
                .addItem(bookId, 2)
                .build();

        assertEquals(userId, order.getUserId());
        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(bookId, order.getItems().get(0).getBookId());
        assertEquals(2, order.getItems().get(0).getQuantity());
    }
}
