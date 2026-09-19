package com.bookstore.repository;

import com.bookstore.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> { }
