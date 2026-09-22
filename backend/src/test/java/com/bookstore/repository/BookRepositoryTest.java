package com.bookstore.repository;

import com.bookstore.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.bookstore.support.BookFixtures;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class BookRepositoryTest {
    @Autowired
    private BookRepository repository;

    @Test
    void retrievesAvailableBooksFromDatabase() {
        repository.save(BookFixtures.availableBook());
        repository.save(BookFixtures.unavailableBook());

        var availableBooks = repository.findByInventoryStockQuantityGreaterThan(BookFixtures.OUT_OF_STOCK);
        assertEquals(1, availableBooks.size());
        assertEquals(BookFixtures.TITLE, availableBooks.get(0).getTitle());
    }
}
