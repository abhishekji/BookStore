package com.bookstore.application.checkout;

import com.bookstore.domain.Book;
import java.math.BigDecimal;

/** Calculates the unit price captured by an order item at checkout time. */
public interface PricingStrategy {
    BigDecimal unitPriceFor(Book book);
}
