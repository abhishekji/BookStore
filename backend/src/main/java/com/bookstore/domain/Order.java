package com.bookstore.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "customer_orders", indexes = @Index(name = "idx_customer_orders_user_created", columnList = "user_id, created_at"))
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();
    protected Order() { }
    public Order(UUID userId) { this.userId = Objects.requireNonNull(userId); }
    public static Builder builder() { return new Builder(); }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public void addItem(UUID bookId, String bookTitle, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("Items can only be added to a placed order");
        }
        items.add(new OrderItem(bookId, bookTitle, quantity, unitPrice));
    }
    public BigDecimal calculateTotal() {
        return items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void confirm() {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("Only a placed order can be confirmed");
        }
        status = OrderStatus.CONFIRMED;
    }
    public enum OrderStatus { PLACED, CONFIRMED, PAID, FULFILLED, CANCELLED }

    public static final class Builder {
        private UUID userId;
        private Instant createdAt = Instant.now();
        private final List<OrderItem> items = new ArrayList<>();

        public Builder forUser(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required");
            return this;
        }

        public Builder addItem(UUID bookId, String bookTitle, int quantity, BigDecimal unitPrice) {
            this.items.add(new OrderItem(bookId, bookTitle, quantity, unitPrice));
            return this;
        }

        public Order build() {
            Order order = new Order(Objects.requireNonNull(userId, "User ID is required"));
            order.createdAt = createdAt;
            order.items.addAll(items);
            return order;
        }
    }
}
