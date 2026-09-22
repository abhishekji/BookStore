package com.bookstore.repository;

import com.bookstore.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("select o from Order o where o.user.id = :userId order by o.createdAt desc")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
}
