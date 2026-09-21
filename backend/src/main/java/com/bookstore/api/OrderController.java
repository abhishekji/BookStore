package com.bookstore.api;

import com.bookstore.application.checkout.CheckoutApplicationService;
import com.bookstore.domain.UserAccount;
import com.bookstore.dto.OrderDtos;
import com.bookstore.repository.UserAccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Authenticated checkout and order history APIs")
public class OrderController {
    private final CheckoutApplicationService service;
    private final UserAccountRepository users;

    public OrderController(CheckoutApplicationService service, UserAccountRepository users) {
        this.service = service; this.users = users;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout the current cart", description = "Requires an Idempotency-Key header.")
    public ResponseEntity<OrderDtos.OrderResponse> checkout(Authentication authentication,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var result = service.checkout(userId(authentication), idempotencyKey);
        return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(result.order());
    }

    @GetMapping
    public List<OrderDtos.OrderResponse> orders(Authentication authentication) {
        return service.findOrders(userId(authentication));
    }

    @GetMapping("/{orderId}")
    public OrderDtos.OrderResponse order(Authentication authentication, @PathVariable UUID orderId) {
        return service.findOrder(userId(authentication), orderId);
    }

    private UUID userId(Authentication authentication) {
        return users.findByEmail(authentication.getName()).map(UserAccount::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
    }
}
