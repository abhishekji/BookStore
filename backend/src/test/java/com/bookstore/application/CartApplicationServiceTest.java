package com.bookstore.application;

import com.bookstore.domain.Cart;
import com.bookstore.infrastructure.BusinessEventLogger;
import com.bookstore.repository.CartRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class CartApplicationServiceTest {
    private final CartRepository repository = mock(CartRepository.class);
    private final BusinessEventLogger eventLogger = mock(BusinessEventLogger.class);
    private final CartApplicationService service = new CartApplicationService(repository, eventLogger);
    private final UUID userId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();

    @Test
    void logsSuccessfulAddToCartWithResultingQuantity() {
        when(repository.findByUserId(userId)).thenReturn(Optional.of(new Cart(userId)));

        service.addItem(userId, bookId, 2);

        verify(eventLogger).cartItemAdded(userId, bookId, 2, 2);
        verify(repository).save(any(Cart.class));
    }

    @Test
    void logsSuccessfulRemoveFromCart() {
        Cart cart = new Cart(userId);
        cart.addItem(bookId, 1);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(cart));

        service.removeItem(userId, bookId);

        verify(eventLogger).cartItemRemoved(userId, bookId, true);
    }
}
