package com.bookstore.domain;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID bookId;
    private int quantity;
    protected CartItem() { }
    CartItem(UUID bookId, int quantity) {
        this.bookId = Objects.requireNonNull(bookId);
        setQuantity(quantity);
    }
    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public int getQuantity() { return quantity; }
    void increase(int amount) { quantity += amount; }
    void setQuantity(int quantity) {
        if (quantity < QuantityRules.MINIMUM_POSITIVE_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }
}
