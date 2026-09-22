package com.bookstore.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Book extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank
    private String title;
    @NotBlank
    private String author;
    private String isbn;
    @NotNull
    @DecimalMin(value = BookRules.MINIMUM_PRICE)
    private BigDecimal price;
    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private Inventory inventory;

    protected Book() { }

    public Book(String title, String author, String isbn, BigDecimal price, int stockQuantity) {
        if (title == null || title.isBlank() || author == null || author.isBlank()) {
            throw new IllegalArgumentException("Title and author are required");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.title = title.trim();
        this.author = author.trim();
        this.isbn = isbn;
        this.price = price;
        this.inventory = new Inventory(stockQuantity);
        this.inventory.assignBook(this);
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public BigDecimal getPrice() { return price; }
    public Inventory getInventory() { return inventory; }
    public int getStockQuantity() { return inventory.getStockQuantity(); }
    public boolean isInStock() { return inventory.isInStock(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id != null && Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
