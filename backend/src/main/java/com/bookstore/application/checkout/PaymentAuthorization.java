package com.bookstore.application.checkout;

public record PaymentAuthorization(String reference, PaymentMethod method) {
}
