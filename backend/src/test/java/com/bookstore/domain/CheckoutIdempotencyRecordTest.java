package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutIdempotencyRecordTest {
    @Test
    void startsProcessingAndCanBeCompletedOnce() {
        CheckoutIdempotencyRecord record = new CheckoutIdempotencyRecord(
                UUID.randomUUID(), "checkout-key", "fingerprint");
        UUID orderId = UUID.randomUUID();

        record.complete(orderId, Instant.now());

        assertEquals(CheckoutIdempotencyStatus.COMPLETED, record.getStatus());
        assertEquals(orderId, record.getOrderId());
        assertThrows(IllegalStateException.class,
                () -> record.complete(UUID.randomUUID(), Instant.now()));
    }

    @Test
    void rejectsMissingClaimValues() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new CheckoutIdempotencyRecord(null, "key", "fingerprint")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new CheckoutIdempotencyRecord(UUID.randomUUID(), " ", "fingerprint")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new CheckoutIdempotencyRecord(UUID.randomUUID(), "key", " "))
        );
    }
}
