package com.bookstore.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "cart", uniqueConstraints = @UniqueConstraint(name = "uk_cart_user", columnNames = "user_id"))
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Version
    private long version;
    private UUID userId;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CartItem> items = new ArrayList<>();
    protected Cart() { }
    public Cart(UUID userId) { this.userId = Objects.requireNonNull(userId); }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public List<CartItem> getItems() { return List.copyOf(items); }
    public void addItem(UUID bookId, int quantity) {
        if (quantity < QuantityRules.MINIMUM_POSITIVE_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        items.stream().filter(i -> i.getBookId().equals(bookId)).findFirst()
                .ifPresentOrElse(i -> i.increase(quantity), () -> items.add(new CartItem(bookId, quantity)));
    }
    public void changeQuantity(UUID bookId, int quantity) {
        findItem(bookId).setQuantity(quantity);
    }
    public UUID resolveBookId(UUID selector) {
        return items.stream()
                .filter(item -> selector.equals(item.getBookId()) || selector.equals(item.getId()))
                .map(CartItem::getBookId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cart item not found"));
    }
    public void removeItem(UUID bookId) {
        boolean removed = items.removeIf(item -> item.getBookId().equals(bookId));
        if (!removed) {
            throw new NoSuchElementException("Cart item not found");
        }
    }
    public void clear() { items.clear(); }
    public BigDecimal calculateTotal(Map<UUID, BigDecimal> prices) {
        return items.stream()
                .map(item -> prices.getOrDefault(item.getBookId(), BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private CartItem findItem(UUID bookId) {
        return items.stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cart item not found"));
    }
}
