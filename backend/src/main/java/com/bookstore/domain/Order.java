package com.bookstore.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "customer_orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING)
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
    public void addItem(UUID bookId, int quantity) { items.add(new OrderItem(bookId, quantity)); }
    public enum OrderStatus { PLACED, PAID, FULFILLED, CANCELLED }

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

        public Builder addItem(UUID bookId, int quantity) {
            this.items.add(new OrderItem(bookId, quantity));
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
