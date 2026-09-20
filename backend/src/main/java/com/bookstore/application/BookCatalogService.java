package com.bookstore.application;

import com.bookstore.dto.BookResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.domain.BookRules;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BookCatalogService {
    private final BookRepository books;
    public BookCatalogService(BookRepository books) { this.books = books; }
    public List<BookResponse> listBooks() {
        return books.findAll().stream().map(BookResponse::from).toList();
    }
    public List<BookResponse> listAvailableBooks() {
        return books.findByInventoryStockQuantityGreaterThan(BookRules.OUT_OF_STOCK_QUANTITY).stream()
                .map(BookResponse::from).toList();
    }
    public BookResponse getBook(UUID id) {
        return books.findById(id).map(BookResponse::from)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
