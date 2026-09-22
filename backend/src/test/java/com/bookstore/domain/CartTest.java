package com.bookstore.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CartTest {
    @Test
    void combinesRepeatedBookItems() {
        Cart cart = new Cart(UUID.randomUUID());
        UUID book = UUID.randomUUID();
        cart.addItem(book, 1);
        cart.addItem(book, 2);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }
}
