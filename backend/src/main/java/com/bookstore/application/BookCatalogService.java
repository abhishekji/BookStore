package com.bookstore.application;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookResponse;
import com.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookCatalogService {
    private final BookRepository books;
    public BookCatalogService(BookRepository books) { this.books = books; }
    public List<BookResponse> listBooks() {
        return books.findAll().stream().map(BookResponse::from).toList();
    }
}
