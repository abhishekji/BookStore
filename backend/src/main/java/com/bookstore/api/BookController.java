package com.bookstore.api;

import com.bookstore.application.BookCatalogService;
import com.bookstore.dto.BookResponse;
import com.bookstore.infrastructure.ApiRoutes;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({ApiRoutes.BOOKS, ApiRoutes.VERSIONED_BOOKS})
public class BookController {
    private final BookCatalogService catalog;
    public BookController(BookCatalogService catalog) { this.catalog = catalog; }
    @GetMapping
    public List<BookResponse> listBooks() { return catalog.listAvailableBooks(); }

    @GetMapping("/{id}")
    public BookResponse getBook(@PathVariable UUID id) { return catalog.getBook(id); }
}
