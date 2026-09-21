package com.bookstore.application.checkout;

import com.bookstore.domain.*;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckoutApplicationServiceTest {
    private final CartRepository carts = mock(CartRepository.class);
    private final BookRepository books = mock(BookRepository.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final CheckoutIdempotencyService idempotency = mock(CheckoutIdempotencyService.class);
    private final PricingStrategy pricing = mock(PricingStrategy.class);
    private final CheckoutApplicationService service = new CheckoutApplicationService(carts, books, orders, idempotency, pricing);
    private final UUID userId = UUID.randomUUID();

    @Test
    void createsConfirmedSnapshotOrderAndClearsCart() {
        UUID bookId = UUID.randomUUID();
        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);
        Book book = mock(Book.class);
        Inventory inventory = mock(Inventory.class);
        when(item.getBookId()).thenReturn(bookId); when(item.getQuantity()).thenReturn(2);
        when(cart.getItems()).thenReturn(List.of(item));
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));
        when(book.getId()).thenReturn(bookId); when(book.getTitle()).thenReturn("Clean Code"); when(book.getInventory()).thenReturn(inventory);
        when(books.findAllByIdForUpdate(List.of(bookId))).thenReturn(List.of(book));
        when(pricing.unitPriceFor(book)).thenReturn(new BigDecimal("29.99"));
        when(idempotency.begin(userId, "key", "checkout-v1")).thenReturn(CheckoutIdempotencyResult.newCheckout());
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.checkout(userId, "key");

        var saved = org.mockito.ArgumentCaptor.forClass(Order.class);
        verify(orders).save(saved.capture());
        assertEquals(Order.OrderStatus.CONFIRMED, saved.getValue().getStatus());
        assertEquals("Clean Code", saved.getValue().getItems().get(0).getBookTitle());
        assertEquals(new BigDecimal("29.99"), saved.getValue().getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("59.98"), saved.getValue().calculateTotal());
        verify(inventory).reserve(2); verify(cart).clear(); verify(idempotency).complete(eq(userId), eq("key"), isNull());
    }

    @Test
    void rejectsEmptyCartWithoutClearingOrPersisting() {
        Cart cart = mock(Cart.class);
        when(idempotency.begin(userId, "key", "checkout-v1")).thenReturn(CheckoutIdempotencyResult.newCheckout());
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.checkout(userId, "key"));
        verify(cart, never()).clear(); verifyNoInteractions(orders);
    }

    @Test
    void missingBookLeavesCartUntouchedAndDoesNotPersistOrder() {
        UUID bookId = UUID.randomUUID(); Cart cart = mock(Cart.class); CartItem item = mock(CartItem.class);
        when(item.getBookId()).thenReturn(bookId); when(item.getQuantity()).thenReturn(1); when(cart.getItems()).thenReturn(List.of(item));
        when(idempotency.begin(userId, "key", "checkout-v1")).thenReturn(CheckoutIdempotencyResult.newCheckout());
        when(carts.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));
        when(books.findAllByIdForUpdate(List.of(bookId))).thenReturn(List.of());
        assertThrows(BookNotFoundException.class, () -> service.checkout(userId, "key"));
        verify(cart, never()).clear(); verifyNoInteractions(orders);
    }
}
