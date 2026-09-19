package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartAdditionalTest {
    @Test
    void rejectsNullUser() {
        assertThrows(NullPointerException.class, () -> new Cart(null));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        Cart cart = new Cart(UUID.randomUUID());
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> cart.addItem(UUID.randomUUID(), 0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> cart.addItem(UUID.randomUUID(), -1))
        );
    }

    @Test
    void removesOnlyTheRequestedBook() {
        Cart cart = new Cart(UUID.randomUUID());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        cart.addItem(first, 1);
        cart.addItem(second, 2);

        cart.removeItem(first);

        assertEquals(1, cart.getItems().size());
        assertEquals(second, cart.getItems().get(0).getBookId());
    }

    @Test
    void exposesImmutableItemsView() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addItem(UUID.randomUUID(), 1);

        assertThrows(UnsupportedOperationException.class,
                () -> cart.getItems().clear());
    }
}
