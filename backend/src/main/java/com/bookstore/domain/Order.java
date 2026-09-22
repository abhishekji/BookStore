package com.bookstore.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "customer_orders", indexes = @Index(name = "idx_customer_orders_user_created", columnList = "user_id, created_at"))
public class Order extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();
    protected Order() { }
    public Order(UserAccount user) { this.user = Objects.requireNonNull(user, "User is required"); }
    public Order(UUID userId) { this(UserAccount.reference(userId)); }
    public static Builder builder() { return new Builder(); }
    public UUID getId() { return id; }
    public UserAccount getUser() { return user; }
    public UUID getUserId() { return user.getId(); }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public void addItem(UUID bookId, String bookTitle, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("Items can only be added to a placed order");
        }
        items.add(new OrderItem(bookId, bookTitle, quantity, unitPrice));
        touch();
    }
    public BigDecimal calculateTotal() {
        return items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void confirm() {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("Only a placed order can be confirmed");
        }
        status = OrderStatus.CONFIRMED;
        touch();
    }
    public enum OrderStatus { PLACED, CONFIRMED, PAID, FULFILLED, CANCELLED }

    public static final class Builder {
        private UserAccount user;
        private java.time.Instant createdAt = java.time.Instant.now();
        private final List<OrderItem> items = new ArrayList<>();

        public Builder forUser(UserAccount user) {
            this.user = user;
            return this;
        }

        public Builder forUser(UUID userId) {
            return forUser(UserAccount.reference(userId));
        }

        public Builder createdAt(java.time.Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required");
            return this;
        }

        public Builder addItem(UUID bookId, String bookTitle, int quantity, BigDecimal unitPrice) {
            this.items.add(new OrderItem(bookId, bookTitle, quantity, unitPrice));
            return this;
        }

        public Order build() {
            Order order = new Order(Objects.requireNonNull(user, "User is required"));
            order.setCreatedAt(createdAt);
            order.items.addAll(items);
            return order;
        }
    }
}
