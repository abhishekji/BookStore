package com.bookstore.application;

import com.bookstore.domain.UserAccount;
import com.bookstore.dto.AuthDtos;
import com.bookstore.infrastructure.JwtService;
import com.bookstore.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthApplicationServiceTest {
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthApplicationService service =
            new AuthApplicationService(users, encoder, authenticationManager, jwtService);

    @Test
    void registersWithHashedPassword() {
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("StrongPass1")).thenReturn("$2a$hashed");
        when(jwtService.generate("reader@example.com")).thenReturn("token");

        var result = service.register(new AuthDtos.RegisterRequest(
                "reader@example.com", "StrongPass1", "Reader"));

        verify(encoder).encode("StrongPass1");
        verify(users).save(argThat(user -> !user.getPasswordHash().equals("StrongPass1")
                && user.getPasswordHash().equals("$2a$hashed")));
        assertEquals("token", result.token());
    }

    @Test
    void authenticatesValidCredentials() {
        UserAccount account = new UserAccount("reader@example.com", "$2a$hashed", "Reader");
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        User.withUsername("reader@example.com").password("$2a$hashed").roles("USER").build(),
                        null));
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.of(account));
        when(jwtService.generate("reader@example.com")).thenReturn("token");

        assertEquals("token", service.login(new AuthDtos.LoginRequest(
                "reader@example.com", "StrongPass1")).token());
    }
}
