package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void startsPlacedAndCanContainItems() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Order order = new Order(userId);
        order.addItem(bookId, "Clean Code", 2, new BigDecimal("29.99"));

        assertEquals(userId, order.getUserId());
        assertEquals(Order.OrderStatus.PLACED, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertEquals(1, order.getItems().size());
        assertEquals(bookId, order.getItems().get(0).getBookId());
        assertEquals("Clean Code", order.getItems().get(0).getBookTitle());
        assertEquals(2, order.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("29.99"), order.getItems().get(0).getUnitPrice());
    }

    @Test
    void calculatesLineAndOrderTotals() {
        Order order = new Order(UUID.randomUUID());
        order.addItem(UUID.randomUUID(), "Alpha", 2, new BigDecimal("10.00"));
        order.addItem(UUID.randomUUID(), "Beta", 3, new BigDecimal("5.50"));

        assertEquals(new BigDecimal("20.00"), order.getItems().get(0).getLineTotal());
        assertEquals(new BigDecimal("16.50"), order.getItems().get(1).getLineTotal());
        assertEquals(new BigDecimal("36.50"), order.calculateTotal());
    }

    @Test
    void preservesPurchaseTimePriceSnapshot() {
        UUID bookId = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID());

        order.addItem(bookId, "Snapshot Book", 1, new BigDecimal("12.34"));

        assertEquals(new BigDecimal("12.34"), order.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("12.34"), order.getItems().get(0).getLineTotal());
    }

    @Test
    void rejectsNullUser() {
        assertThrows(NullPointerException.class, () -> new Order((UUID) null));
    }

    @Test
    void rejectsInvalidItemQuantity() {
        Order order = new Order(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class,
                () -> order.addItem(UUID.randomUUID(), "X", 0, new BigDecimal("10.00")));
    }

    @Test
    void rejectsInvalidUnitPrice() {
        Order order = new Order(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class,
                () -> order.addItem(UUID.randomUUID(), "X", 1, new BigDecimal("-1.00")));
    }

    @Test
    void confirmsOrderFromPlacedState() {
        Order order = new Order(UUID.randomUUID());
        order.addItem(UUID.randomUUID(), "X", 1, new BigDecimal("10.00"));

        order.confirm();

        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Order order = new Order(UUID.randomUUID());
        order.confirm();

        assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    void exposesImmutableItemsView() {
        Order order = new Order(UUID.randomUUID());
        order.addItem(UUID.randomUUID(), "X", 1, new BigDecimal("10.00"));

        assertThrows(UnsupportedOperationException.class,
                () -> order.getItems().clear());
    }
}
