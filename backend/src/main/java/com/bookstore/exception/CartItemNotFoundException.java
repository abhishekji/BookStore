package com.bookstore.exception;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(UUID bookId) {
        super("Cart item not found for book: " + bookId);
    }
}
