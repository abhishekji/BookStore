package com.bookstore.dto;

import com.bookstore.domain.Book;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BookResponseTest {
    @Test
    void mapsBookFieldsToResponse() {
        Book book = new Book("Clean Code", "Robert Martin", "9780132350884",
                BigDecimal.valueOf(39.99), 4);

        BookResponse response = BookResponse.from(book);

        assertEquals(book.getId(), response.id());
        assertEquals(book.getTitle(), response.title());
        assertEquals(book.getAuthor(), response.author());
        assertEquals(book.getIsbn(), response.isbn());
        assertEquals(book.getPrice(), response.price());
        assertEquals(book.getStockQuantity(), response.stockQuantity());
    }
}
