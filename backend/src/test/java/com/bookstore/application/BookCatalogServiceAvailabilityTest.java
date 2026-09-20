package com.bookstore.application;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.support.BookFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCatalogServiceAvailabilityTest {
    private final BookRepository repository = mock(BookRepository.class);
    private final BookCatalogService service = new BookCatalogService(repository);

    @Test
    void returnsOnlyAvailableBooks() {
        Book available = BookFixtures.availableBook();
        when(repository.findByInventoryStockQuantityGreaterThan(BookFixtures.OUT_OF_STOCK))
                .thenReturn(List.of(available));

        List<BookResponse> result = service.listAvailableBooks();

        assertEquals(1, result.size());
        assertEquals(BookFixtures.TITLE, result.get(0).title());
        verify(repository).findByInventoryStockQuantityGreaterThan(BookFixtures.OUT_OF_STOCK);
    }

    @Test
    void returnsBookById() {
        UUID id = UUID.randomUUID();
        Book book = new Book("Title", "Author", null, BigDecimal.TEN, 1);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        assertEquals("Title", service.getBook(id).title());
    }

    @Test
    void throwsNotFoundWhenBookDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.getBook(id));
    }
}
