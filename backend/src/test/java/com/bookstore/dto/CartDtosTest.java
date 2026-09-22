package com.bookstore.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartDtosTest {
    @Test
    void cartRequestAndResponseValuesRetainCartLineDetails() {
        UUID cartId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        CartDtos.AddCartItemRequest add = new CartDtos.AddCartItemRequest(bookId, 2);
        CartDtos.ChangeCartItemRequest change = new CartDtos.ChangeCartItemRequest(3);
        CartDtos.CartItemResponse item = new CartDtos.CartItemResponse(cartItemId, bookId, "Clean Code", 2,
                new BigDecimal("10.00"), new BigDecimal("20.00"));
        CartDtos.CartResponse cart = new CartDtos.CartResponse(cartId, List.of(item), new BigDecimal("20.00"));

        assertEquals(bookId, add.bookId());
        assertEquals(2, add.quantity());
        assertEquals(3, change.quantity());
        assertEquals(cartItemId, cart.items().get(0).cartItemId());
        assertEquals(new BigDecimal("20.00"), cart.total());
    }
}
