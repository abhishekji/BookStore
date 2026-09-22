package com.bookstore.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import com.bookstore.support.BookFixtures;

class BookTest {
    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book(BookFixtures.TITLE, BookFixtures.AUTHOR, BookFixtures.ISBN,
                        BigDecimal.valueOf(-1), BookFixtures.AVAILABLE_STOCK));
    }
    @Test
    void trimsRequiredText() {
        Book book = new Book(" " + BookFixtures.TITLE + " ", " " + BookFixtures.AUTHOR + " ",
                null, BigDecimal.TEN, BookFixtures.AVAILABLE_STOCK);
        assertEquals(BookFixtures.TITLE, book.getTitle());
        assertEquals(BookFixtures.AUTHOR, book.getAuthor());
    }

    @Test
    void exposesAvailabilityFromStock() {
        assertTrue(BookFixtures.availableBook().isInStock());
        assertFalse(BookFixtures.unavailableBook().isInStock());
    }

    @Test
    void rejectsMissingRequiredFieldsAndNullPrice() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Book(null, "Author", null, BigDecimal.TEN, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Book("Title", " ", null, BigDecimal.TEN, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Book("Title", "Author", null, null, 1))
        );
    }
}
