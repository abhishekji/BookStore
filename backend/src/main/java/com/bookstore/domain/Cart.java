package com.bookstore.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CartItem> items = new ArrayList<>();
    protected Cart() { }
    public Cart(UUID userId) { this.userId = Objects.requireNonNull(userId); }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public List<CartItem> getItems() { return List.copyOf(items); }
    public void addItem(UUID bookId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        items.stream().filter(i -> i.getBookId().equals(bookId)).findFirst()
                .ifPresentOrElse(i -> i.increase(quantity), () -> items.add(new CartItem(bookId, quantity)));
    }
    public void removeItem(UUID bookId) { items.removeIf(item -> item.getBookId().equals(bookId)); }
}
