package com.bookstore.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "checkout_idempotency",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_checkout_idempotency_user_key",
                columnNames = {"user_id", "idempotency_key"}))
public class CheckoutIdempotencyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "idempotency_key", nullable = false, length = CheckoutIdempotencyRules.MAX_KEY_LENGTH)
    private String idempotencyKey;

    @Column(name = "request_fingerprint", nullable = false,
            length = CheckoutIdempotencyRules.MAX_FINGERPRINT_LENGTH)
    private String requestFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CheckoutIdempotencyStatus status;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected CheckoutIdempotencyRecord() {
    }

    public CheckoutIdempotencyRecord(UUID userId, String idempotencyKey, String requestFingerprint) {
        this.userId = Objects.requireNonNull(userId, "User ID is required");
        this.idempotencyKey = requireValue(idempotencyKey, CheckoutIdempotencyRules.MAX_KEY_LENGTH,
                "Idempotency key is required");
        this.requestFingerprint = requireValue(requestFingerprint,
                CheckoutIdempotencyRules.MAX_FINGERPRINT_LENGTH, "Request fingerprint is required");
        this.status = CheckoutIdempotencyStatus.PROCESSING;
        this.createdAt = Instant.now();
    }

    public void complete(UUID orderId, Instant completedAt) {
        if (status != CheckoutIdempotencyStatus.PROCESSING) {
            throw new IllegalStateException("Checkout idempotency record is already completed");
        }
        this.orderId = Objects.requireNonNull(orderId, "Order ID is required");
        this.completedAt = Objects.requireNonNull(completedAt, "Completion time is required");
        this.status = CheckoutIdempotencyStatus.COMPLETED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestFingerprint() {
        return requestFingerprint;
    }

    public CheckoutIdempotencyStatus getStatus() {
        return status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    private static String requireValue(String value, int maxLength, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(message + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }
}
