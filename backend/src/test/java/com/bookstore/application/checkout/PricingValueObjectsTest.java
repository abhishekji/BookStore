package com.bookstore.application.checkout;

import com.bookstore.domain.Book;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingValueObjectsTest {
    @Test
    void standardPricingUsesCurrentCataloguePrice() {
        Book book = new Book("Clean Code", "Robert Martin", null, new BigDecimal("19.95"), 1);

        assertEquals(new BigDecimal("19.95"), new StandardPricingStrategy().unitPriceFor(book));
    }
}
