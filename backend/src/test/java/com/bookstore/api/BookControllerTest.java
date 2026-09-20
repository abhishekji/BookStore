package com.bookstore.api;

import com.bookstore.application.BookCatalogService;
import com.bookstore.dto.BookResponse;
import com.bookstore.dto.BookPageResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bookstore.support.BookFixtures;

class BookControllerTest {
    @Test
    void delegatesBookListingToCatalogService() {
        BookCatalogService catalog = mock(BookCatalogService.class);
        BookResponse response = BookFixtures.availableResponse();
        when(catalog.searchAvailableBooks("", 0, 5)).thenReturn(
                new BookPageResponse(List.of(response), 0, 5, false, 1));

        BookPageResponse result = new BookController(catalog).listBooks(0, 5, "");

        assertEquals(List.of(response), result.content());
        verify(catalog).searchAvailableBooks("", 0, 5);
    }
}
