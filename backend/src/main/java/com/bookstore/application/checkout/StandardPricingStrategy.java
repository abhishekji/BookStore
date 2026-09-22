package com.bookstore.application.checkout;

import com.bookstore.domain.Book;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/** Current catalogue pricing policy. New policies can replace this without changing checkout orchestration. */
@Component
public class StandardPricingStrategy implements PricingStrategy {
    @Override public BigDecimal unitPriceFor(Book book) { return book.getPrice(); }
}
