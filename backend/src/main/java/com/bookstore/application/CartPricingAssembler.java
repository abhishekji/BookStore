package com.bookstore.application;

import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.dto.CartDtos;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CartPricingAssembler {
    private final BookRepository books;

    public CartPricingAssembler(BookRepository books) {
        this.books = books;
    }

    public PricedCart assemble(Cart cart) {
        List<UUID> bookIds = cart.getItems().stream().map(CartItem::getBookId).toList();
        Map<UUID, Book> booksById = books.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        return assemble(cart, booksById, Book::getPrice);
    }

    public PricedCart assemble(Cart cart, Map<UUID, Book> booksById, Function<Book, BigDecimal> priceForBook) {
        List<PricedCartLine> lines = cart.getItems().stream().map(item -> {
            Book book = booksById.get(item.getBookId());
            if (book == null) {
                throw new BookNotFoundException(item.getBookId());
            }
            BigDecimal unitPrice = Objects.requireNonNull(priceForBook.apply(book), "Book price is required");
            return new PricedCartLine(item, book, unitPrice);
        }).toList();
        return new PricedCart(cart.getId(), lines);
    }

    public record PricedCart(UUID cartId, List<PricedCartLine> lines) {
        public CartDtos.CartResponse toResponse() {
            List<CartDtos.CartItemResponse> items = lines.stream().map(line ->
                    new CartDtos.CartItemResponse(line.cartItem().getId(), line.book().getId(),
                            line.book().getTitle(), line.cartItem().getQuantity(), line.unitPrice(),
                            line.lineTotal())).toList();
            BigDecimal total = items.stream().map(CartDtos.CartItemResponse::lineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new CartDtos.CartResponse(cartId, items, total);
        }
    }

    public record PricedCartLine(CartItem cartItem, Book book, BigDecimal unitPrice) {
        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        }
    }
}
