package com.bookstore.domain;

public final class UserAccountRules {
    public static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private UserAccountRules() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }
}
