package com.bookstore.api;

import com.bookstore.application.BookCatalogService;
import com.bookstore.dto.BookResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookCatalogService catalog;
    public BookController(BookCatalogService catalog) { this.catalog = catalog; }
    @GetMapping
    public List<BookResponse> listBooks() { return catalog.listBooks(); }
}
