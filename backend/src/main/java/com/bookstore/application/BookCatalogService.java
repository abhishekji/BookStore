package com.bookstore.application;

import com.bookstore.dto.BookResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.domain.Book;
import com.bookstore.domain.BookRules;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bookstore.dto.BookPageResponse;
import com.bookstore.infrastructure.OffsetBasedPageRequest;

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
    public BookPageResponse searchAvailableBooks(String search, int offset, int limit) {
        int safeOffset = Math.max(offset, 0);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        String normalizedSearch = search == null ? "" : search.trim();
        Pageable pageable = new OffsetBasedPageRequest(safeOffset, safeLimit,
                org.springframework.data.domain.Sort.by("title").ascending());
        Page<Book> page = books.searchAvailableBooks(BookRules.OUT_OF_STOCK_QUANTITY, normalizedSearch, pageable);
        return new BookPageResponse(page.getContent().stream().map(BookResponse::from).toList(),
                safeOffset, safeLimit, page.hasNext(), page.getTotalElements());
    }
    public BookResponse getBook(UUID id) {
        return books.findById(id).map(BookResponse::from)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
