package com.bookstore.domain;

public final class CheckoutIdempotencyRules {
    public static final int MAX_KEY_LENGTH = 128;
    public static final int MAX_FINGERPRINT_LENGTH = 128;

    private CheckoutIdempotencyRules() {
    }
}
