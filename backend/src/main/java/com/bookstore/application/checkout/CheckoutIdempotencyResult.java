package com.bookstore.application.checkout;

import java.util.UUID;

public record CheckoutIdempotencyResult(boolean replayed, UUID orderId) {
    public static CheckoutIdempotencyResult newCheckout() {
        return new CheckoutIdempotencyResult(false, null);
    }

    public static CheckoutIdempotencyResult replay(UUID orderId) {
        return new CheckoutIdempotencyResult(true, orderId);
    }
}
