package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditableEntityTest {
    @Test
    void tracksCreationAndUpdatesForAllCoreEntities() {
        UserAccount user = UserAccount.reference(UUID.randomUUID());
        Book book = new Book("Clean Code", "Robert Martin", null, BigDecimal.TEN, 1);
        Cart cart = new Cart(user);
        cart.addItem(UUID.randomUUID(), 1);
        CartItem cartItem = cart.getItems().get(0);
        Order order = Order.builder().forUser(user)
                .addItem(UUID.randomUUID(), "Clean Code", 1, BigDecimal.TEN)
                .build();
        OrderItem orderItem = order.getItems().get(0);

        assertNotNull(user.getCreatedAt());
        assertNotNull(book.getCreatedAt());
        assertNotNull(cart.getCreatedAt());
        assertNotNull(cartItem.getCreatedAt());
        assertNotNull(order.getCreatedAt());
        assertNotNull(orderItem.getCreatedAt());

        var updatedAt = order.getUpdatedAt();
        order.confirm();
        assertFalse(order.getUpdatedAt().isBefore(updatedAt));
    }
}
