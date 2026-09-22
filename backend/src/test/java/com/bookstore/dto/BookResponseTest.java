package com.bookstore.dto;

import com.bookstore.domain.Book;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import com.bookstore.support.BookFixtures;

class BookResponseTest {
    @Test
    void mapsBookFieldsToResponse() {
        Book book = BookFixtures.availableBook();

        BookResponse response = BookResponse.from(book);

        assertEquals(book.getId(), response.id());
        assertEquals(book.getTitle(), response.title());
        assertEquals(book.getAuthor(), response.author());
        assertEquals(book.getIsbn(), response.isbn());
        assertEquals(book.getPrice(), response.price());
        assertEquals(book.getStockQuantity(), response.stockQuantity());
        assertEquals(book.isInStock(), response.inStock());
    }
}
