package com.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CartDtos {
    private CartDtos() {
    }

    public record AddCartItemRequest(@NotNull UUID bookId, @Min(1) int quantity) {
    }

    public record ChangeCartItemRequest(@Min(1) int quantity) {
    }

    public record CartItemResponse(UUID cartItemId, UUID bookId, String title, int quantity,
                                   BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public record CartResponse(UUID id, List<CartItemResponse> items, BigDecimal total) {
    }
}
