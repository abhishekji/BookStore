package com.bookstore.application.checkout;

import java.util.Objects;
import java.util.UUID;

/**
 * Data carried through checkout steps. Step implementations own workflow state changes.
 */
public final class CheckoutContext {
    private final UUID userId;
    private final String idempotencyKey;
    private UUID orderId;

    public CheckoutContext(UUID userId, String idempotencyKey) {
        this.userId = Objects.requireNonNull(userId, "User ID is required");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        this.idempotencyKey = idempotencyKey.trim();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void assignOrder(UUID orderId) {
        this.orderId = Objects.requireNonNull(orderId, "Order ID is required");
    }
}
