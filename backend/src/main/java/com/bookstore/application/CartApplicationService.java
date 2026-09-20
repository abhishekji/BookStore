package com.bookstore.application;

import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.infrastructure.BusinessEventLogger;
import com.bookstore.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartApplicationService {
    private final CartRepository repository;
    private final BusinessEventLogger eventLogger;

    public CartApplicationService(CartRepository repository, BusinessEventLogger eventLogger) {
        this.repository = repository;
        this.eventLogger = eventLogger;
    }

    @Transactional
    public void addItem(UUID userId, UUID bookId, int quantity) {
        Cart cart = repository.findByUserId(userId).orElseGet(() -> new Cart(userId));
        cart.addItem(bookId, quantity);
        repository.save(cart);
        int resultingQuantity = cart.getItems().stream()
                .filter(item -> item.getBookId().equals(bookId))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElseThrow();
        eventLogger.cartItemAdded(userId, bookId, quantity, resultingQuantity);
    }

    @Transactional
    public void removeItem(UUID userId, UUID bookId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        boolean removed = cart.getItems().stream().anyMatch(item -> item.getBookId().equals(bookId));
        cart.removeItem(bookId);
        repository.save(cart);
        eventLogger.cartItemRemoved(userId, bookId, removed);
    }
}
