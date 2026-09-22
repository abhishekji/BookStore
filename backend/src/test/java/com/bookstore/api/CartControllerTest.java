package com.bookstore.api;

import com.bookstore.application.CartApplicationService;
import com.bookstore.domain.UserAccount;
import com.bookstore.dto.CartDtos;
import com.bookstore.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartControllerTest {
    private final CartApplicationService service = mock(CartApplicationService.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final CartController controller = new CartController(service, users);
    private final UUID userId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();
    private final TestingAuthenticationToken authentication = new TestingAuthenticationToken("reader@example.com", "password");

    @Test
    void delegatesAllCartMutationsForTheAuthenticatedAccount() {
        UserAccount user = mock(UserAccount.class);
        when(user.getId()).thenReturn(userId);
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        CartDtos.CartResponse cart = new CartDtos.CartResponse(UUID.randomUUID(), List.of(), BigDecimal.ZERO);
        when(service.getCart(userId)).thenReturn(cart);

        assertSame(cart, controller.getCart(authentication));
        assertSame(cart, controller.addItem(authentication, new CartDtos.AddCartItemRequest(bookId, 2)));
        assertSame(cart, controller.changeQuantity(authentication, bookId, new CartDtos.ChangeCartItemRequest(3)));
        controller.removeItem(authentication, bookId);

        verify(service).addItem(userId, bookId, 2);
        verify(service).changeQuantity(userId, bookId, 3);
        verify(service).removeItem(userId, bookId);
        verify(service, times(3)).getCart(userId);
    }

    @Test
    void rejectsRequestsWhenTheAuthenticatedAccountWasDeleted() {
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.getCart(authentication));
    }
}
