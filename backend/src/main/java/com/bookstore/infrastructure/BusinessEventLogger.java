package com.bookstore.infrastructure;

import java.util.UUID;

public interface BusinessEventLogger {
    void cartItemAdded(UUID userId, UUID bookId, int quantity, int resultingQuantity);

    void cartItemRemoved(UUID userId, UUID bookId, boolean removed);

    void checkoutStarted(UUID userId);

    void checkoutCompleted(UUID userId, UUID orderId);

    void checkoutReplayed(UUID userId, UUID orderId);

    void checkoutRejected(UUID userId, String outcome);

}
