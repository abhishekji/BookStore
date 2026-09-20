package com.bookstore.api;

import com.bookstore.application.CartApplicationService;
import com.bookstore.dto.CartDtos;
import com.bookstore.domain.UserAccount;
import com.bookstore.repository.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Authenticated shopping cart APIs")
public class CartController {
    private final CartApplicationService service;
    private final UserAccountRepository users;

    public CartController(CartApplicationService service, UserAccountRepository users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping
    @Operation(summary = "Get the current user's cart")
    public CartDtos.CartResponse getCart(Authentication authentication) {
        return service.getCart(userId(authentication));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a book to the current user's cart")
    @ResponseStatus(HttpStatus.CREATED)
    public CartDtos.CartResponse addItem(Authentication authentication,
                                          @Valid @RequestBody CartDtos.AddCartItemRequest request) {
        UUID userId = userId(authentication);
        service.addItem(userId, request.bookId(), request.quantity());
        return service.getCart(userId);
    }

    @PatchMapping("/items/{bookId}")
    @Operation(summary = "Change cart item quantity",
            description = "The path value may be the book ID returned in the cart response. Existing cart-item IDs are also accepted for compatibility.")
    public CartDtos.CartResponse changeQuantity(Authentication authentication, @PathVariable UUID bookId,
                                                @Valid @RequestBody CartDtos.ChangeCartItemRequest request) {
        UUID userId = userId(authentication);
        service.changeQuantity(userId, bookId, request.quantity());
        return service.getCart(userId);
    }

    @DeleteMapping("/items/{bookId}")
    @Operation(summary = "Remove a book from the current user's cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(Authentication authentication, @PathVariable UUID bookId) {
        service.removeItem(userId(authentication), bookId);
    }

    private UUID userId(Authentication authentication) {
        return users.findByEmail(authentication.getName()).map(UserAccount::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
    }
}
