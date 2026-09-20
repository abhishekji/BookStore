package com.bookstore.infrastructure;

import com.bookstore.domain.UserAccount;
import com.bookstore.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DatabaseUserDetailsServiceTest {
    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final DatabaseUserDetailsService service = new DatabaseUserDetailsService(repository);

    @Test
    void loadsUserByNormalizedEmail() {
        UserAccount account = new UserAccount("reader@example.com", "{bcrypt}hash", "Reader");
        when(repository.findByEmail("reader@example.com")).thenReturn(Optional.of(account));

        var details = service.loadUserByUsername(" READER@EXAMPLE.COM ");

        assertEquals("reader@example.com", details.getUsername());
        assertEquals("{bcrypt}hash", details.getPassword());
        verify(repository).findByEmail("reader@example.com");
    }

    @Test
    void rejectsUnknownUserWithoutExposingAccountDetails() {
        when(repository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        var exception = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown@example.com"));

        assertEquals("Invalid username or password", exception.getMessage());
    }
}
