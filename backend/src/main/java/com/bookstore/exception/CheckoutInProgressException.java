package com.bookstore.exception;

public class CheckoutInProgressException extends RuntimeException {
    public CheckoutInProgressException() {
        super("A checkout with this idempotency key is already in progress");
    }
}
