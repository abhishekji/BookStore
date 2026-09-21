package com.bookstore.application.checkout;

import com.bookstore.domain.Book;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingValueObjectsTest {
    @Test
    void standardPricingUsesCurrentCataloguePriceAndAuthorizationRetainsPaymentMethod() {
        Book book = new Book("Clean Code", "Robert Martin", null, new BigDecimal("19.95"), 1);
        PaymentAuthorization authorization = new PaymentAuthorization("payment-1", PaymentMethod.CARD);

        assertEquals(new BigDecimal("19.95"), new StandardPricingStrategy().unitPriceFor(book));
        assertEquals("payment-1", authorization.reference());
        assertEquals(PaymentMethod.CARD, authorization.method());
    }
}
