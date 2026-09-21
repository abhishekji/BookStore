package com.bookstore.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import org.slf4j.MDC;

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
        log.info("event=checkout_started correlationId={} userId={} outcome=processing", correlationId(), userId);
    }

    @Override
    public void checkoutCompleted(UUID userId, UUID orderId) {
        log.info("event=checkout_completed correlationId={} userId={} orderId={} outcome=success", correlationId(), userId, orderId);
    }

    @Override
    public void checkoutReplayed(UUID userId, UUID orderId) {
        log.info("event=checkout_replayed correlationId={} userId={} orderId={} outcome=duplicate_request", correlationId(), userId, orderId);
    }

    @Override
    public void checkoutRejected(UUID userId, String outcome) {
        log.info("event=checkout_rejected correlationId={} userId={} outcome={}", correlationId(), userId, outcome);
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
    private String correlationId() { return MDC.get("correlationId"); }
}
