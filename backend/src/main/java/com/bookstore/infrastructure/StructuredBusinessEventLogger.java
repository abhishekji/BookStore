package com.bookstore.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StructuredBusinessEventLogger implements BusinessEventLogger {
    private static final Logger log = LoggerFactory.getLogger(StructuredBusinessEventLogger.class);

    @Override
    public void cartItemAdded(UUID userId, UUID bookId, int quantity, int resultingQuantity) {
        log.info("event=cart_item_added userId={} bookId={} quantity={} resultingQuantity={} outcome=success",
                userId, bookId, quantity, resultingQuantity);
    }

    @Override
    public void cartItemRemoved(UUID userId, UUID bookId, boolean removed) {
        log.info("event=cart_item_removed userId={} bookId={} removed={} outcome={}",
                userId, bookId, removed, removed ? "success" : "not_found");
    }

    @Override
    public void checkoutStarted(UUID userId) {
        log.info("event=checkout_started userId={} outcome=processing", userId);
    }

    @Override
    public void checkoutCompleted(UUID userId, UUID orderId) {
        log.info("event=checkout_completed userId={} orderId={} outcome=success", userId, orderId);
    }

    @Override
    public void checkoutReplayed(UUID userId, UUID orderId) {
        log.info("event=checkout_replayed userId={} orderId={} outcome=duplicate_request", userId, orderId);
    }

    @Override
    public void checkoutRejected(UUID userId, String outcome) {
        log.info("event=checkout_rejected userId={} outcome={}", userId, outcome);
    }

    @Override
    public void sagaStep(String event, String saga, String step) {
        log.info("event={} saga={} step={}", event, saga, step);
    }

    @Override
    public void sagaCompensationFailed(String saga, String step, RuntimeException failure) {
        log.error("event={} saga={} step={} outcome=failed",
                SagaLogEvents.COMPENSATION_FAILED, saga, step, failure);
    }
}
