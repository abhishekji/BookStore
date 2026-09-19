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
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public void addItem(UUID bookId, int quantity) { items.add(new OrderItem(bookId, quantity)); }
    public enum OrderStatus { PLACED, PAID, FULFILLED, CANCELLED }
}
