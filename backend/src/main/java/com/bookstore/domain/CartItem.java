package com.bookstore.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID bookId;
    private int quantity;
    protected CartItem() { }
    CartItem(UUID bookId, int quantity) { this.bookId = bookId; this.quantity = quantity; }
    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public int getQuantity() { return quantity; }
    void increase(int amount) { quantity += amount; }
}
