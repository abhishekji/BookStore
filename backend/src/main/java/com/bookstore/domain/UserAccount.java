package com.bookstore.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import java.util.UUID;

@Entity
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String email;
    private String passwordHash;
    private String displayName;

    protected UserAccount() { }

    public UserAccount(String email, String passwordHash, String displayName) {
        if (email == null || email.isBlank() || passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.displayName = displayName == null ? "" : displayName.trim();
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
}
