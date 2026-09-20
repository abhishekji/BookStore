package com.bookstore.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID bookId;
    private int quantity;
    protected OrderItem() { }
    OrderItem(UUID bookId, int quantity) {
        if (quantity < QuantityRules.MINIMUM_POSITIVE_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.bookId = bookId; this.quantity = quantity;
    }
    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public int getQuantity() { return quantity; }
}
