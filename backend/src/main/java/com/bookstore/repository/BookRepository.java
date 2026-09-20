package com.bookstore.repository;

import com.bookstore.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID> {
    List<Book> findByInventoryStockQuantityGreaterThan(int minimumStock);

    @Query(value = """
            select b from Book b join b.inventory i
            where i.stockQuantity > :minimumStock
              and (:search = '' or lower(b.title) like lower(concat('%', :search, '%')))
            """,
            countQuery = """
            select count(b) from Book b join b.inventory i
            where i.stockQuantity > :minimumStock
              and (:search = '' or lower(b.title) like lower(concat('%', :search, '%')))
            """)
    Page<Book> searchAvailableBooks(@Param("minimumStock") int minimumStock,
                                    @Param("search") String search,
                                    Pageable pageable);
}
