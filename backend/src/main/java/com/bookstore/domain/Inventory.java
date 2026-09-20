package com.bookstore.domain;

import jakarta.persistence.*;

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
}
