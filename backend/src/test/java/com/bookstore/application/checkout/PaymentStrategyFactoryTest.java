package com.bookstore.application.checkout;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentStrategyFactoryTest {
    @Test
    void selectsStrategyForPaymentMethod() {
        PaymentStrategy cardStrategy = strategyFor(PaymentMethod.CARD);
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(cardStrategy));

        assertSame(cardStrategy, factory.forMethod(PaymentMethod.CARD));
    }

    @Test
    void rejectsUnsupportedPaymentMethod() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> factory.forMethod(PaymentMethod.CASH_ON_DELIVERY));
    }

    @Test
    void normalizesAndValidatesPaymentRequest() {
        PaymentRequest request = new PaymentRequest(
                PaymentMethod.CARD, new BigDecimal("12.50"), " usd ");

        org.junit.jupiter.api.Assertions.assertEquals("USD", request.currency());
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRequest(PaymentMethod.CARD, new BigDecimal("-1"), "USD"));
    }

    private PaymentStrategy strategyFor(PaymentMethod method) {
        return new PaymentStrategy() {
            @Override public boolean supports(PaymentMethod candidate) { return candidate == method; }
            @Override public PaymentAuthorization authorize(PaymentRequest request) {
                return new PaymentAuthorization("reference", method);
            }
            @Override public void refund(PaymentAuthorization authorization) {
            }
        };
    }
}
