package com.bookstore.application;

import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.dto.CartDtos;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.CartItemNotFoundException;
import com.bookstore.exception.CartNotFoundException;
import com.bookstore.infrastructure.BusinessEventLogger;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartApplicationServiceBehaviorTest {
    private final CartRepository carts = mock(CartRepository.class);
    private final BookRepository books = mock(BookRepository.class);
    private final BusinessEventLogger events = mock(BusinessEventLogger.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final CartApplicationService service =
            new CartApplicationService(carts, books, events, users, new CartPricingAssembler(books));
    private final UUID userId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();

    @Test
    void rejectsMissingAndUnavailableBooksBeforeCreatingCart() {
        when(books.findById(bookId)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> service.addItem(userId, bookId, 1));

        Book unavailable = mock(Book.class);
        when(books.findById(bookId)).thenReturn(Optional.of(unavailable));
        when(unavailable.isInStock()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.addItem(userId, bookId, 1));
        verifyNoInteractions(carts, events);
    }

    @Test
    void createsCartAndReturnsPricedCartContents() {
        Book book = mock(Book.class);
        UUID cartId = UUID.randomUUID();
        Cart cart = spy(new Cart(userId));
        cart.addItem(bookId, 2);
        doReturn(cartId).when(cart).getId();
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());
        when(users.getReferenceById(userId)).thenReturn(com.bookstore.domain.UserAccount.reference(userId));
        when(books.findById(bookId)).thenReturn(Optional.of(book));
        when(book.isInStock()).thenReturn(true);
        when(carts.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addItem(userId, bookId, 2);
        verify(carts).save(any(Cart.class));
        verify(events).cartItemAdded(userId, bookId, 2, 2);

        when(carts.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(books.findAllById(any())).thenReturn(java.util.List.of(book));
        when(book.getId()).thenReturn(bookId);
        when(book.getTitle()).thenReturn("Clean Code");
        when(book.getPrice()).thenReturn(new BigDecimal("12.50"));
        CartDtos.CartResponse response = service.getCart(userId);

        assertEquals(cartId, response.id());
        assertEquals(new BigDecimal("25.00"), response.total());
        assertEquals("Clean Code", response.items().get(0).title());
    }

    @Test
    void createsEmptyCartWhenReadingForFirstTime() {
        Cart cart = new Cart(userId);
        when(carts.findByUserId(userId)).thenReturn(Optional.empty());
        when(users.getReferenceById(userId)).thenReturn(com.bookstore.domain.UserAccount.reference(userId));
        when(carts.save(any(Cart.class))).thenReturn(cart);

        CartDtos.CartResponse response = service.getCart(userId);

        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.total());
        verify(books).findAllById(java.util.List.of());
    }

    @Test
    void reportsMissingCatalogEntryWhilePricingCart() {
        Cart cart = new Cart(userId);
        cart.addItem(bookId, 1);
        when(carts.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(books.findAllById(any())).thenReturn(java.util.List.of());

        assertThrows(BookNotFoundException.class, () -> service.getCart(userId));
    }

    @Test
    void changesAndRemovesItemsByBookIdentifier() {
        Cart cart = new Cart(userId);
        cart.addItem(bookId, 1);
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));

        service.changeQuantity(userId, bookId, 3);
        assertEquals(3, cart.getItems().get(0).getQuantity());
        verify(events).cartItemQuantityChanged(userId, bookId, 1, 3);
        service.removeItem(userId, bookId);

        assertTrue(cart.getItems().isEmpty());
        verify(carts, times(2)).save(cart);
        verify(events).cartItemRemoved(userId, bookId, true);
    }

    @Test
    void translatesAbsentCartAndItemSelectorsToCartExceptions() {
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> service.changeQuantity(userId, bookId, 1));
        assertThrows(CartNotFoundException.class, () -> service.removeItem(userId, bookId));

        Cart cart = new Cart(userId);
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));
        assertThrows(CartItemNotFoundException.class, () -> service.changeQuantity(userId, bookId, 1));
        assertThrows(CartItemNotFoundException.class, () -> service.removeItem(userId, bookId));
    }
}
