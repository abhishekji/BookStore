package com.bookstore.repository;

import com.bookstore.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> { }
