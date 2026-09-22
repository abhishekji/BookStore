package com.bookstore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@MappedSuperclass
public abstract class AuditableEntity {
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Instant updatedAt = createdAt;

    @PrePersist
    protected void initializeAuditTimestamps() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void updateAuditTimestamp() {
        touch();
    }

    protected final void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    protected final void touch() {
        updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
