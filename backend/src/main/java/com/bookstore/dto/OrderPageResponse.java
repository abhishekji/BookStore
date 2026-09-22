package com.bookstore.dto;

import java.util.List;

public record OrderPageResponse(
        List<OrderDtos.OrderResponse> content,
        int offset,
        int limit,
        boolean hasNext,
        long total
) {
}
