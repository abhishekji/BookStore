package com.bookstore.application.checkout;

import com.bookstore.dto.OrderDtos;

public record CheckoutResult(OrderDtos.OrderResponse order, boolean replayed) { }
