package com.bookstore.application.checkout;

import com.bookstore.domain.CheckoutIdempotencyRecord;
import com.bookstore.domain.CheckoutIdempotencyStatus;
import com.bookstore.exception.CheckoutInProgressException;
import com.bookstore.exception.IdempotencyConflictException;
import com.bookstore.repository.CheckoutIdempotencyRepository;
import com.bookstore.infrastructure.BusinessEventLogger;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutIdempotencyServiceTest {
    private final CheckoutIdempotencyRepository repository = mock(CheckoutIdempotencyRepository.class);
    private final CheckoutIdempotencyService service =
            new CheckoutIdempotencyService(repository, mock(BusinessEventLogger.class));
    private final UUID userId = UUID.randomUUID();
    private static final String KEY = "checkout-key";
    private static final String FINGERPRINT = "request-fingerprint";

    @Test
    void claimsNewKey() {
        when(repository.findByUserIdAndIdempotencyKey(userId, KEY)).thenReturn(Optional.empty());

        CheckoutIdempotencyResult result = service.begin(userId, KEY, " " + FINGERPRINT + " ");

        assertFalse(result.replayed());
        assertNull(result.orderId());
        verify(repository).saveAndFlush(any(CheckoutIdempotencyRecord.class));
    }

    @Test
    void replaysCompletedCheckoutForSameRequest() {
        UUID orderId = UUID.randomUUID();
        CheckoutIdempotencyRecord record = completedRecord(orderId);
        when(repository.findByUserIdAndIdempotencyKey(userId, KEY)).thenReturn(Optional.of(record));

        CheckoutIdempotencyResult result = service.begin(userId, KEY, FINGERPRINT);

        assertTrue(result.replayed());
        assertEquals(orderId, result.orderId());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsKeyReuseForDifferentRequest() {
        when(repository.findByUserIdAndIdempotencyKey(userId, KEY))
                .thenReturn(Optional.of(new CheckoutIdempotencyRecord(userId, KEY, FINGERPRINT)));

        assertThrows(IdempotencyConflictException.class,
                () -> service.begin(userId, KEY, "different-fingerprint"));
    }

    @Test
    void rejectsConcurrentProcessingForSameRequest() {
        when(repository.findByUserIdAndIdempotencyKey(userId, KEY))
                .thenReturn(Optional.of(new CheckoutIdempotencyRecord(userId, KEY, FINGERPRINT)));

        assertThrows(CheckoutInProgressException.class,
                () -> service.begin(userId, KEY, FINGERPRINT));
    }

    @Test
    void databaseUniquenessConflictIsReportedAsInProgress() {
        when(repository.findByUserIdAndIdempotencyKey(userId, KEY)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(CheckoutIdempotencyRecord.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(CheckoutInProgressException.class,
                () -> service.begin(userId, KEY, FINGERPRINT));
    }

    @Test
    void completesClaimWithOrderId() {
        CheckoutIdempotencyRecord record = new CheckoutIdempotencyRecord(userId, KEY, FINGERPRINT);
        when(repository.findByUserIdAndIdempotencyKey(userId, KEY)).thenReturn(Optional.of(record));
        UUID orderId = UUID.randomUUID();

        service.complete(userId, KEY, orderId);

        assertEquals(CheckoutIdempotencyStatus.COMPLETED, record.getStatus());
        assertEquals(orderId, record.getOrderId());
        verify(repository).save(record);
    }

    private CheckoutIdempotencyRecord completedRecord(UUID orderId) {
        CheckoutIdempotencyRecord record = new CheckoutIdempotencyRecord(userId, KEY, FINGERPRINT);
        record.complete(orderId, java.time.Instant.now());
        return record;
    }
}
