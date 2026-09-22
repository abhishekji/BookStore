package com.bookstore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    protected Inventory() {
    }

    Inventory(int stockQuantity) {
        if (stockQuantity < BookRules.OUT_OF_STOCK_QUANTITY) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stockQuantity = stockQuantity;
    }

    void assignBook(Book book) {
        this.book = Objects.requireNonNull(book, "Book is required");
    }

    public UUID getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isInStock() {
        return stockQuantity >= BookRules.MINIMUM_AVAILABLE_QUANTITY;
    }
    public void reserve(int quantity) {
        if (quantity < QuantityRules.MINIMUM_POSITIVE_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stockQuantity < quantity) {
            throw new IllegalArgumentException("Book is currently unavailable in the requested quantity");
        }
        stockQuantity -= quantity;
    }
}
