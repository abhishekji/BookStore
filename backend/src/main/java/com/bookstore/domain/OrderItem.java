package com.bookstore.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Entity
public class OrderItem extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID bookId;
    @Column(nullable = false)
    private String bookTitle;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private int quantity;
    protected OrderItem() { }
    OrderItem(UUID bookId, String bookTitle, int quantity, BigDecimal unitPrice) {
        this.bookId = Objects.requireNonNull(bookId, "Book ID is required");
        if (bookTitle == null || bookTitle.isBlank()) {
            throw new IllegalArgumentException("Book title is required");
        }
        if (quantity < QuantityRules.MINIMUM_POSITIVE_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.bookTitle = bookTitle.trim();
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }
    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
