package com.bookstore.application.checkout;

import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Order;
import com.bookstore.application.CartPricingAssembler;
import com.bookstore.dto.OrderDtos;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.CartNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CheckoutApplicationService {
    private static final String CHECKOUT_FINGERPRINT = "checkout-v1";
    private final CartRepository carts;
    private final BookRepository books;
    private final OrderRepository orders;
    private final CheckoutIdempotencyService idempotency;
    private final PricingStrategy pricing;
    private final UserAccountRepository users;
    private final CartPricingAssembler cartPricingAssembler;

    public CheckoutApplicationService(CartRepository carts, BookRepository books, OrderRepository orders,
                                      CheckoutIdempotencyService idempotency, PricingStrategy pricing,
                                      UserAccountRepository users, CartPricingAssembler cartPricingAssembler) {
        this.carts = carts; this.books = books; this.orders = orders;
        this.idempotency = idempotency; this.pricing = pricing; this.users = users;
        this.cartPricingAssembler = cartPricingAssembler;
    }

    @Transactional
    public CheckoutResult checkout(UUID userId, String idempotencyKey) {
        CheckoutIdempotencyResult claim = idempotency.begin(userId, idempotencyKey, CHECKOUT_FINGERPRINT);
        if (claim.replayed()) {
            return new CheckoutResult(OrderDtos.OrderResponse.from(orders.findById(claim.orderId()).orElseThrow()), true);
        }
        Cart cart = carts.findByUserIdForUpdate(userId).orElseThrow(() -> new CartNotFoundException(userId));
        if (cart.getItems().isEmpty()) throw new IllegalArgumentException("Cart cannot be empty");

        List<UUID> bookIds = cart.getItems().stream().map(CartItem::getBookId).sorted(Comparator.naturalOrder()).toList();
        Map<UUID, Book> booksById = books.findAllByIdForUpdate(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        Order.Builder orderBuilder = Order.builder().forUser(users.getReferenceById(userId));
        CartPricingAssembler.PricedCart pricedCart =
                cartPricingAssembler.assemble(cart, booksById, pricing::unitPriceFor);
        for (CartPricingAssembler.PricedCartLine line : pricedCart.lines()) {
            line.book().getInventory().reserve(line.cartItem().getQuantity());
            orderBuilder.addItem(line.book().getId(), line.book().getTitle(),
                    line.cartItem().getQuantity(), line.unitPrice());
        }
        Order order = orderBuilder.build();
        order.confirm();
        Order persisted = orders.save(order);
        cart.clear();
        idempotency.complete(userId, idempotencyKey, persisted.getId());
        return new CheckoutResult(OrderDtos.OrderResponse.from(persisted), false);
    }

}
