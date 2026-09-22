package com.bookstore.repository;

import com.bookstore.domain.CheckoutIdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckoutIdempotencyRepository
        extends JpaRepository<CheckoutIdempotencyRecord, UUID> {
    Optional<CheckoutIdempotencyRecord> findByUserIdAndIdempotencyKey(
            UUID userId, String idempotencyKey);
}
