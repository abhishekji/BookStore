package com.bookstore.dto;

import com.bookstore.domain.Order;
import com.bookstore.domain.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() { }
    public record OrderItemResponse(UUID bookId, String bookTitle, int quantity,
                                    BigDecimal unitPrice, BigDecimal lineTotal) { }
    public record OrderResponse(UUID id, List<OrderItemResponse> items, BigDecimal total,
                                Order.OrderStatus status, Instant createdAt, Instant updatedAt) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getItems().stream()
                    .map(item -> new OrderItemResponse(item.getBookId(), item.getBookTitle(), item.getQuantity(),
                            item.getUnitPrice(), item.getLineTotal())).toList(),
                    order.calculateTotal(), order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
        }
    }
}
