package com.bookstore.api;

import com.bookstore.application.checkout.CheckoutApplicationService;
import com.bookstore.application.orders.OrderQueryService;
import com.bookstore.domain.UserAccount;
import com.bookstore.dto.OrderDtos;
import com.bookstore.dto.OrderPageResponse;
import com.bookstore.repository.UserAccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Authenticated checkout and order history APIs")
public class OrderController {
    private final CheckoutApplicationService checkoutService;
    private final OrderQueryService orderQueryService;
    private final UserAccountRepository users;

    public OrderController(CheckoutApplicationService checkoutService, OrderQueryService orderQueryService,
                           UserAccountRepository users) {
        this.checkoutService = checkoutService;
        this.orderQueryService = orderQueryService;
        this.users = users;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout the current cart", description = "Requires an Idempotency-Key header.")
    public ResponseEntity<OrderDtos.OrderResponse> checkout(Authentication authentication,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        var result = checkoutService.checkout(userId(authentication), idempotencyKey);
        return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(result.order());
    }

    @GetMapping
    @Operation(summary = "List the authenticated reader's orders",
            description = "Returns orders in descending creation order with offset pagination.")
    public OrderPageResponse orders(Authentication authentication,
                                    @RequestParam(defaultValue = "0") int offset,
                                    @RequestParam(defaultValue = "10") int limit) {
        return orderQueryService.findOrders(userId(authentication), offset, limit);
    }

    @GetMapping("/{orderId}")
    public OrderDtos.OrderResponse order(Authentication authentication, @PathVariable UUID orderId) {
        return orderQueryService.findOrder(userId(authentication), orderId);
    }

    private UUID userId(Authentication authentication) {
        return users.findByEmail(authentication.getName()).map(UserAccount::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
    }
}
