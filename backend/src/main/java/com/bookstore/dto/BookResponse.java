package com.bookstore.dto;

import com.bookstore.domain.Book;
import java.math.BigDecimal;
import java.util.UUID;

public record BookResponse(UUID id, String title, String author, String isbn, BigDecimal price,
                           int stockQuantity, boolean inStock) {
    public BookResponse(UUID id, String title, String author, String isbn, BigDecimal price, int stockQuantity) {
        this(id, title, author, isbn, price, stockQuantity, stockQuantity > 0);
    }

    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPrice(), book.getStockQuantity(), book.isInStock());
    }
}
