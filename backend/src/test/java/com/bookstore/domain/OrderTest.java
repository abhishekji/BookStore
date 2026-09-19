package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void startsPlacedAndCanContainItems() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Order order = new Order(userId);
        order.addItem(bookId, 2);

        assertEquals(userId, order.getUserId());
        assertEquals(Order.OrderStatus.PLACED, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertEquals(1, order.getItems().size());
        assertEquals(bookId, order.getItems().get(0).getBookId());
        assertEquals(2, order.getItems().get(0).getQuantity());
    }

    @Test
    void rejectsNullUser() {
        assertThrows(NullPointerException.class, () -> new Order(null));
    }

    @Test
    void rejectsInvalidItemQuantity() {
        Order order = new Order(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class,
                () -> order.addItem(UUID.randomUUID(), 0));
    }

    @Test
    void exposesImmutableItemsView() {
        Order order = new Order(UUID.randomUUID());
        order.addItem(UUID.randomUUID(), 1);

        assertThrows(UnsupportedOperationException.class,
                () -> order.getItems().clear());
    }
}
