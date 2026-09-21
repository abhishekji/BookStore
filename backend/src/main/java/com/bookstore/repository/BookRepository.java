package com.bookstore.repository;

import com.bookstore.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface BookRepository extends JpaRepository<Book, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b join fetch b.inventory where b.id in :ids order by b.id")
    List<Book> findAllByIdForUpdate(@Param("ids") List<UUID> ids);
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
