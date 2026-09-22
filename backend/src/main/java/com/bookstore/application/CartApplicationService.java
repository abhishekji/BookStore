package com.bookstore.application;

import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Book;
import com.bookstore.dto.CartDtos;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.CartItemNotFoundException;
import com.bookstore.exception.CartNotFoundException;
import com.bookstore.infrastructure.BusinessEventLogger;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartApplicationService {
    private final CartRepository repository;
    private final BookRepository books;
    private final BusinessEventLogger eventLogger;
    private final UserAccountRepository users;
    private final CartPricingAssembler cartPricingAssembler;

    public CartApplicationService(CartRepository repository, BookRepository books, BusinessEventLogger eventLogger,
                                  UserAccountRepository users, CartPricingAssembler cartPricingAssembler) {
        this.repository = repository;
        this.books = books;
        this.eventLogger = eventLogger;
        this.users = users;
        this.cartPricingAssembler = cartPricingAssembler;
    }

    @Transactional
    public void addItem(UUID userId, UUID bookId, int quantity) {
        Book book = books.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        if (!book.isInStock()) {
            throw new IllegalArgumentException("Book is currently unavailable");
        }
        Cart cart = repository.findByUserIdForUpdate(userId)
                .orElseGet(() -> new Cart(users.getReferenceById(userId)));
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
    public CartDtos.CartResponse getCart(UUID userId) {
        Cart cart = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(users.getReferenceById(userId))));
        return cartPricingAssembler.assemble(cart).toResponse();
    }

    @Transactional
    public void changeQuantity(UUID userId, UUID bookId, int quantity) {
        Cart cart = findCart(userId);
        UUID actualBookId = resolveBookId(cart, bookId);
        int previousQuantity = cart.getItems().stream()
                .filter(item -> item.getBookId().equals(actualBookId))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElseThrow();
        try {
            cart.changeQuantity(actualBookId, quantity);
        } catch (java.util.NoSuchElementException exception) {
            throw new CartItemNotFoundException(bookId);
        }
        repository.save(cart);
        eventLogger.cartItemQuantityChanged(userId, actualBookId, previousQuantity, quantity);
    }

    @Transactional
    public void removeItem(UUID userId, UUID bookId) {
        Cart cart = findCart(userId);
        UUID actualBookId = resolveBookId(cart, bookId);
        cart.removeItem(actualBookId);
        repository.save(cart);
        eventLogger.cartItemRemoved(userId, bookId, true);
    }

    private Cart findCart(UUID userId) {
        return repository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    private UUID resolveBookId(Cart cart, UUID selector) {
        try {
            return cart.resolveBookId(selector);
        } catch (java.util.NoSuchElementException exception) {
            throw new CartItemNotFoundException(selector);
        }
    }
}
