package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {
    @Test
    void normalizesEmailAndDisplayName() {
        UserAccount account = new UserAccount("  USER@EXAMPLE.COM ", "hash", "  Reader ");

        assertEquals("user@example.com", account.getEmail());
        assertEquals("hash", account.getPasswordHash());
        assertEquals("Reader", account.getDisplayName());
    }

    @Test
    void defaultsMissingDisplayNameToEmpty() {
        assertEquals("", new UserAccount("user@example.com", "hash", null).getDisplayName());
    }

    @Test
    void rejectsMissingEmailOrPassword() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new UserAccount(null, "hash", "Reader")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new UserAccount(" ", "hash", "Reader")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new UserAccount("user@example.com", null, "Reader")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new UserAccount("user@example.com", " ", "Reader"))
        );
    }

    @Test
    void rejectsMalformedEmailAndOversizedDisplayName() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new UserAccount("invalid-email", "hash", "Reader")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new UserAccount("user@example.com", "hash",
                                "x".repeat(UserAccountRules.MAX_DISPLAY_NAME_LENGTH + 1)))
        );
    }
}
