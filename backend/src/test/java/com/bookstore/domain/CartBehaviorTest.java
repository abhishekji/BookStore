package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartBehaviorTest {
    @Test
    void addsSameBookByIncreasingExistingQuantity() {
        Cart cart = new Cart(UUID.randomUUID());
        UUID bookId = UUID.randomUUID();

        cart.addItem(bookId, 1);
        cart.addItem(bookId, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void changesAndRemovesItems() {
        Cart cart = new Cart(UUID.randomUUID());
        UUID bookId = UUID.randomUUID();
        cart.addItem(bookId, 2);

        cart.changeQuantity(bookId, 4);
        assertEquals(4, cart.getItems().get(0).getQuantity());

        cart.removeItem(bookId);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void calculatesEmptyAndPopulatedTotals() {
        Cart cart = new Cart(UUID.randomUUID());
        UUID bookId = UUID.randomUUID();

        assertEquals(BigDecimal.ZERO, cart.calculateTotal(Map.of()));
        cart.addItem(bookId, 2);

        assertEquals(new BigDecimal("19.98"),
                cart.calculateTotal(Map.of(bookId, new BigDecimal("9.99"))));
    }

    @Test
    void rejectsInvalidQuantityAndMissingItem() {
        Cart cart = new Cart(UUID.randomUUID());
        UUID bookId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> cart.addItem(bookId, 0));
        assertThrows(java.util.NoSuchElementException.class,
                () -> cart.changeQuantity(bookId, 2));
    }
}
