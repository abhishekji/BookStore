package com.bookstore.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BookTest {
    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("Clean Code", "Robert Martin", "9780132350884", BigDecimal.valueOf(-1), 1));
    }
    @Test
    void trimsRequiredText() {
        Book book = new Book(" Clean Code ", " Robert Martin ", null, BigDecimal.TEN, 2);
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
    }
}
