package com.bookstore.application.checkout;

import com.bookstore.domain.CheckoutIdempotencyRecord;
import com.bookstore.domain.CheckoutIdempotencyRules;
import com.bookstore.domain.CheckoutIdempotencyStatus;
import com.bookstore.exception.CheckoutInProgressException;
import com.bookstore.exception.IdempotencyConflictException;
import com.bookstore.repository.CheckoutIdempotencyRepository;
import com.bookstore.infrastructure.BusinessEventLogger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CheckoutIdempotencyService {
    private final CheckoutIdempotencyRepository repository;
    private final BusinessEventLogger eventLogger;

    public CheckoutIdempotencyService(CheckoutIdempotencyRepository repository,
                                      BusinessEventLogger eventLogger) {
        this.repository = repository;
        this.eventLogger = eventLogger;
    }

    /**
     * Must be called inside the checkout transaction. The unique database constraint
     * is the final guard when two application instances claim the same key together.
     */
    @Transactional
    public CheckoutIdempotencyResult begin(UUID userId, String idempotencyKey,
                                           String requestFingerprint) {
        CheckoutContext context = new CheckoutContext(userId, idempotencyKey);
        String normalizedFingerprint = requireFingerprint(requestFingerprint);

        CheckoutIdempotencyResult result = repository.findByUserIdAndIdempotencyKey(
                        context.getUserId(), context.getIdempotencyKey())
                .map(existing -> resolveExisting(existing, normalizedFingerprint))
                .orElseGet(() -> createClaim(context, normalizedFingerprint));
        if (result.replayed()) {
            eventLogger.checkoutReplayed(userId, result.orderId());
        } else {
            eventLogger.checkoutStarted(userId);
        }
        return result;
    }

    @Transactional
    public void complete(UUID userId, String idempotencyKey, UUID orderId) {
        CheckoutIdempotencyRecord record = findRecord(userId, idempotencyKey);
        record.complete(orderId, Instant.now());
        repository.save(record);
        eventLogger.checkoutCompleted(userId, orderId);
    }

    private CheckoutIdempotencyResult createClaim(CheckoutContext context, String requestFingerprint) {
        try {
            repository.saveAndFlush(new CheckoutIdempotencyRecord(
                    context.getUserId(), context.getIdempotencyKey(), requestFingerprint));
            return CheckoutIdempotencyResult.newCheckout();
        } catch (DataIntegrityViolationException exception) {
            eventLogger.checkoutRejected(context.getUserId(), "already_processing");
            throw new CheckoutInProgressException();
        }
    }

    private CheckoutIdempotencyResult resolveExisting(CheckoutIdempotencyRecord existing,
                                                       String requestFingerprint) {
        if (!existing.getRequestFingerprint().equals(requestFingerprint)) {
            eventLogger.checkoutRejected(existing.getUserId(), "fingerprint_mismatch");
            throw new IdempotencyConflictException(
                    "The idempotency key was already used for a different checkout request");
        }
        if (existing.getStatus() == CheckoutIdempotencyStatus.PROCESSING) {
            eventLogger.checkoutRejected(existing.getUserId(), "already_processing");
            throw new CheckoutInProgressException();
        }
        return CheckoutIdempotencyResult.replay(existing.getOrderId());
    }

    private CheckoutIdempotencyRecord findRecord(UUID userId, String idempotencyKey) {
        return repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .orElseThrow(() -> new IdempotencyConflictException(
                        "No checkout claim exists for this idempotency key"));
    }

    private String requireFingerprint(String requestFingerprint) {
        if (requestFingerprint == null || requestFingerprint.isBlank()) {
            throw new IllegalArgumentException("Request fingerprint is required");
        }
        String normalized = requestFingerprint.trim();
        if (normalized.length() > CheckoutIdempotencyRules.MAX_FINGERPRINT_LENGTH) {
            throw new IllegalArgumentException("Request fingerprint must not exceed "
                    + CheckoutIdempotencyRules.MAX_FINGERPRINT_LENGTH + " characters");
        }
        return normalized;
    }
}
