package com.bookstore.application;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookPageResponse;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookCatalogServiceSearchTest {
    @Test
    void normalizesSearchAndBoundsPaginationBeforeQueryingRepository() {
        BookRepository books = mock(BookRepository.class);
        BookCatalogService service = new BookCatalogService(books);
        Book book = new Book("Clean Code", "Robert Martin", null, BigDecimal.TEN, 1);
        when(books.searchAvailableBooks(eq(0), eq("clean"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        BookPageResponse result = service.searchAvailableBooks("  clean  ", -10, 99);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(books).searchAvailableBooks(eq(0), eq("clean"), pageable.capture());
        assertEquals(0, pageable.getValue().getOffset());
        assertEquals(50, pageable.getValue().getPageSize());
        assertEquals(List.of("Clean Code"), result.content().stream().map(item -> item.title()).toList());
        assertEquals(0, result.offset());
        assertEquals(50, result.limit());
    }

    @Test
    void acceptsNullSearchAndPreservesValidOffsetAndLimit() {
        BookRepository books = mock(BookRepository.class);
        BookCatalogService service = new BookCatalogService(books);
        Book book = new Book("Domain-Driven Design", "Eric Evans", null, BigDecimal.TEN, 1);
        when(books.searchAvailableBooks(eq(0), eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book), org.springframework.data.domain.PageRequest.of(1, 4), 9));

        BookPageResponse result = service.searchAvailableBooks(null, 7, 4);

        assertEquals(7, result.offset());
        assertEquals(4, result.limit());
        assertTrue(result.hasNext());
        assertEquals(9, result.total());
    }
}
