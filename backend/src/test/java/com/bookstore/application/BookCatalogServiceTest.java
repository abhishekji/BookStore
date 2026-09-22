package com.bookstore.application;

import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

class BookCatalogServiceTest {
    @Test
    void readsBooksFromRepository() {
        BookRepository repository = Mockito.mock(BookRepository.class);
        new BookCatalogService(repository).listBooks();
        verify(repository).findAll();
    }
}
