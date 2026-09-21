package com.bookstore.application.checkout;

import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Order;
import com.bookstore.dto.OrderDtos;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.CartNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
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

    public CheckoutApplicationService(CartRepository carts, BookRepository books, OrderRepository orders,
                                      CheckoutIdempotencyService idempotency, PricingStrategy pricing) {
        this.carts = carts; this.books = books; this.orders = orders;
        this.idempotency = idempotency; this.pricing = pricing;
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
        Order order = new Order(userId);
        for (CartItem cartItem : cart.getItems()) {
            Book book = booksById.get(cartItem.getBookId());
            if (book == null) throw new BookNotFoundException(cartItem.getBookId());
            book.getInventory().reserve(cartItem.getQuantity());
            order.addItem(book.getId(), book.getTitle(), cartItem.getQuantity(), pricing.unitPriceFor(book));
        }
        order.confirm();
        Order persisted = orders.save(order);
        cart.clear();
        idempotency.complete(userId, idempotencyKey, persisted.getId());
        return new CheckoutResult(OrderDtos.OrderResponse.from(persisted), false);
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> findOrders(UUID userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream().map(OrderDtos.OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse findOrder(UUID userId, UUID orderId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new java.util.NoSuchElementException("Order not found"));
        if (!order.getUserId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Order does not belong to the authenticated user");
        return OrderDtos.OrderResponse.from(order);
    }
}
