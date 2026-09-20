package com.bookstore.application.checkout;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentStrategyFactory {
    private final List<PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public PaymentStrategy forMethod(PaymentMethod method) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment strategy configured for " + method));
    }
}
