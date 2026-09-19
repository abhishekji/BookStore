package com.bookstore.api;

import com.bookstore.application.BookCatalogService;
import com.bookstore.dto.BookResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerTest {
    @Test
    void delegatesBookListingToCatalogService() {
        BookCatalogService catalog = mock(BookCatalogService.class);
        BookResponse response = new BookResponse(UUID.randomUUID(), "Title", "Author", null,
                BigDecimal.TEN, 1);
        when(catalog.listBooks()).thenReturn(List.of(response));

        List<BookResponse> result = new BookController(catalog).listBooks();

        assertEquals(List.of(response), result);
        verify(catalog).listBooks();
    }
}
