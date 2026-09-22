package com.bookstore.repository;

import com.bookstore.domain.CheckoutIdempotencyRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class CheckoutIdempotencyRepositoryTest {
    @Autowired private CheckoutIdempotencyRepository repository;

    @Test
    void enforcesOneCheckoutClaimPerUserAndKey() {
        UUID user = UUID.randomUUID();
        repository.saveAndFlush(new CheckoutIdempotencyRecord(user, "retry-key", "checkout-v1"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new CheckoutIdempotencyRecord(user, "retry-key", "checkout-v1")));
    }

    @Test
    void allowsTheSameKeyForAnotherUser() {
        repository.saveAndFlush(new CheckoutIdempotencyRecord(UUID.randomUUID(), "retry-key", "checkout-v1"));
        repository.saveAndFlush(new CheckoutIdempotencyRecord(UUID.randomUUID(), "retry-key", "checkout-v1"));
    }
}
