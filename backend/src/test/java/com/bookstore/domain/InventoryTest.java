package com.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {
    @Test
    void storesStockSeparatelyFromCatalogueDetails() {
        Book book = new Book("Title", "Author", "isbn", BigDecimal.TEN, 4);

        assertNotNull(book.getInventory());
        assertSame(book, book.getInventory().getBook());
        assertEquals(4, book.getInventory().getStockQuantity());
        assertTrue(book.isInStock());
    }

    @Test
    void rejectsNegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("Title", "Author", "isbn", BigDecimal.TEN, -1));
    }
}
