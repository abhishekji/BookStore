package com.bookstore.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "cart", uniqueConstraints = @UniqueConstraint(name = "uk_cart_user", columnNames = "user_id"))
public class Cart extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Version
    private long version;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CartItem> items = new ArrayList<>();
    protected Cart() { }
    public Cart(UserAccount user) { this.user = Objects.requireNonNull(user, "User is required"); }
    public Cart(UUID userId) { this(UserAccount.reference(userId)); }
    public UUID getId() { return id; }
    public UserAccount getUser() { return user; }
    public UUID getUserId() { return user.getId(); }
    public List<CartItem> getItems() { return List.copyOf(items); }
    public void addItem(UUID bookId, int quantity) {
        if (quantity < QuantityRules.MINIMUM_POSITIVE_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        items.stream().filter(i -> i.getBookId().equals(bookId)).findFirst()
                .ifPresentOrElse(i -> i.increase(quantity), () -> items.add(new CartItem(bookId, quantity)));
        touch();
    }
    public void changeQuantity(UUID bookId, int quantity) {
        findItem(bookId).setQuantity(quantity);
        touch();
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
        touch();
    }
    public void clear() {
        items.clear();
        touch();
    }
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
