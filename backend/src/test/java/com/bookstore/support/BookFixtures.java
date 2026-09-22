package com.bookstore.support;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookResponse;

import java.math.BigDecimal;
import java.util.UUID;

public final class BookFixtures {
    public static final String TITLE = "Clean Code";
    public static final String AUTHOR = "Robert Martin";
    public static final String ISBN = "9780132350884";
    public static final BigDecimal PRICE = BigDecimal.valueOf(39.99);
    public static final int AVAILABLE_STOCK = 3;
    public static final int OUT_OF_STOCK = 0;

    private BookFixtures() {
    }

    public static Book availableBook() {
        return new Book(TITLE, AUTHOR, ISBN, PRICE, AVAILABLE_STOCK);
    }

    public static Book unavailableBook() {
        return new Book("Domain-Driven Design", "Eric Evans", "9780321125217",
                BigDecimal.valueOf(59.99), OUT_OF_STOCK);
    }

    public static BookResponse availableResponse() {
        return new BookResponse(UUID.randomUUID(), TITLE, AUTHOR, ISBN, PRICE, AVAILABLE_STOCK, true);
    }
}
