package com.bookstore.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;

@Entity
public class UserAccount extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 254)
    @NotBlank
    @Email
    private String email;
    @Column(nullable = false)
    @NotBlank
    private String passwordHash;
    @Size(max = UserAccountRules.MAX_DISPLAY_NAME_LENGTH)
    private String displayName;

    protected UserAccount() { }

    public UserAccount(String email, String passwordHash, String displayName) {
        if (email == null || email.isBlank() || !UserAccountRules.isValidEmail(email.trim())) {
            throw new IllegalArgumentException("A valid email is required");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("A password hash is required");
        }
        if (displayName != null && displayName.length() > UserAccountRules.MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException("Display name must not exceed "
                    + UserAccountRules.MAX_DISPLAY_NAME_LENGTH + " characters");
        }
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.displayName = displayName == null ? "" : displayName.trim();
    }

    public static UserAccount reference(UUID id) {
        UserAccount account = new UserAccount();
        account.id = Objects.requireNonNull(id, "User ID is required");
        return account;
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
}
