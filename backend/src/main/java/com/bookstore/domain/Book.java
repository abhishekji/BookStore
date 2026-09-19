package com.bookstore.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private int stockQuantity;

    protected Book() { }

    public Book(String title, String author, String isbn, BigDecimal price, int stockQuantity) {
        if (title == null || title.isBlank() || author == null || author.isBlank()) {
            throw new IllegalArgumentException("Title and author are required");
        }
        if (price == null || price.signum() < 0 || stockQuantity < 0) {
            throw new IllegalArgumentException("Price and stock cannot be negative");
        }
        this.title = title.trim();
        this.author = author.trim();
        this.isbn = isbn;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id != null && Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
