package com.bookstore.application.checkout;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutContextTest {
    @Test
    void requiresUserAndIdempotencyKey() {
        UUID userId = UUID.randomUUID();
        CheckoutContext context = new CheckoutContext(userId, " checkout-key ");

        assertEquals(userId, context.getUserId());
        assertEquals("checkout-key", context.getIdempotencyKey());
        assertNull(context.getOrderId());
    }

    @Test
    void rejectsMissingIdempotencyKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new CheckoutContext(UUID.randomUUID(), " "));
    }

    @Test
    void assignsOrderOnlyWhenIdentifierIsValid() {
        CheckoutContext context = new CheckoutContext(UUID.randomUUID(), "checkout-key");
        UUID orderId = UUID.randomUUID();

        context.assignOrder(orderId);

        assertEquals(orderId, context.getOrderId());
        assertThrows(NullPointerException.class, () -> context.assignOrder(null));
    }
}
