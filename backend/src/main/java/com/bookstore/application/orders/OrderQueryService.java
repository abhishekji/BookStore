package com.bookstore.application.orders;

import com.bookstore.domain.Order;
import com.bookstore.dto.OrderDtos;
import com.bookstore.dto.OrderPageResponse;
import com.bookstore.infrastructure.OffsetBasedPageRequest;
import com.bookstore.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderQueryService {
    private static final int MINIMUM_PAGE_SIZE = 1;
    private static final int MAXIMUM_PAGE_SIZE = 50;
    private static final String CREATED_AT_PROPERTY = "createdAt";

    private final OrderRepository orders;

    public OrderQueryService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public OrderPageResponse findOrders(UUID userId, int offset, int limit) {
        int safeOffset = Math.max(offset, 0);
        int safeLimit = Math.min(Math.max(limit, MINIMUM_PAGE_SIZE), MAXIMUM_PAGE_SIZE);
        Pageable pageable = new OffsetBasedPageRequest(safeOffset, safeLimit,
                Sort.by(CREATED_AT_PROPERTY).descending());
        Page<Order> page = orders.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return new OrderPageResponse(page.getContent().stream().map(OrderDtos.OrderResponse::from).toList(),
                safeOffset, safeLimit, page.hasNext(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse findOrder(UUID userId, UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to the authenticated user");
        }
        return OrderDtos.OrderResponse.from(order);
    }
}
