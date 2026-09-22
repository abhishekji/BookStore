package com.bookstore.application;

import com.bookstore.domain.Cart;
import com.bookstore.domain.Book;
import com.bookstore.infrastructure.BusinessEventLogger;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class CartApplicationServiceTest {
    private final CartRepository repository = mock(CartRepository.class);
    private final BookRepository books = mock(BookRepository.class);
    private final BusinessEventLogger eventLogger = mock(BusinessEventLogger.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final CartApplicationService service =
            new CartApplicationService(repository, books, eventLogger, users, new CartPricingAssembler(books));
    private final UUID userId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();

    @Test
    void logsSuccessfulAddToCartWithResultingQuantity() {
        when(repository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(new Cart(userId)));
        when(books.findById(bookId)).thenReturn(Optional.of(
                new Book("Clean Code", "Robert C. Martin", null, java.math.BigDecimal.TEN, 2)));

        service.addItem(userId, bookId, 2);

        verify(eventLogger).cartItemAdded(userId, bookId, 2, 2);
        verify(repository).save(any(Cart.class));
    }

    @Test
    void logsSuccessfulRemoveFromCart() {
        Cart cart = new Cart(userId);
        cart.addItem(bookId, 1);
        when(repository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));

        service.removeItem(userId, bookId);

        verify(eventLogger).cartItemRemoved(userId, bookId, true);
    }
}
