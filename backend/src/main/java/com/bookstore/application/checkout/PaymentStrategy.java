package com.bookstore.application.checkout;

public interface PaymentStrategy {
    boolean supports(PaymentMethod method);

    PaymentAuthorization authorize(PaymentRequest request);

    void refund(PaymentAuthorization authorization);
}
