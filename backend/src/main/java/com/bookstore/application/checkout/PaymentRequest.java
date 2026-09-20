package com.bookstore.application.checkout;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentRequest(PaymentMethod method, BigDecimal amount, String currency) {
    public PaymentRequest {
        Objects.requireNonNull(method, "Payment method is required");
        Objects.requireNonNull(amount, "Payment amount is required");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Payment currency is required");
        }
        currency = currency.trim().toUpperCase();
    }
}
