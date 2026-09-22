package com.bookstore.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("An account with these credentials already exists");
    }
}
