package com.bookstore.api;

import com.bookstore.application.BookCatalogService;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.GlobalExceptionHandler;
import com.bookstore.infrastructure.CorrelationIdFilter;
import com.bookstore.infrastructure.ApiRoutes;
import com.bookstore.support.BookFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerMvcTest {
    private final BookCatalogService service = mock(BookCatalogService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new BookController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new CorrelationIdFilter())
            .build();

    @Test
    void listsAvailableBooksSuccessfully() throws Exception {
        when(service.listAvailableBooks()).thenReturn(List.of(BookFixtures.availableResponse()));

        mvc.perform(get(ApiRoutes.BOOKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(BookFixtures.TITLE))
                .andExpect(jsonPath("$[0].inStock").value(true))
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void retrievesBookByIdentifierSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getBook(id)).thenReturn(
                new BookResponse(id, BookFixtures.TITLE, BookFixtures.AUTHOR, BookFixtures.ISBN,
                        BookFixtures.PRICE, BookFixtures.AVAILABLE_STOCK, true));

        mvc.perform(get(ApiRoutes.BOOKS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void reportsMissingBookWithNotFoundResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getBook(id)).thenThrow(new BookNotFoundException(id));

        mvc.perform(get(ApiRoutes.BOOKS + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value(ApiRoutes.BOOKS + "/" + id));
    }

    @Test
    void reportsMalformedBookIdentifierAsInvalidRequest() throws Exception {
        mvc.perform(get(ApiRoutes.BOOKS + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }
}
