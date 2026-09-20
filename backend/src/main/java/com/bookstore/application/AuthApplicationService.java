package com.bookstore.application;

import com.bookstore.domain.UserAccount;
import com.bookstore.domain.UserAccountRules;
import com.bookstore.dto.AuthDtos;
import com.bookstore.exception.UserAlreadyExistsException;
import com.bookstore.infrastructure.JwtService;
import com.bookstore.repository.UserAccountRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthApplicationService(UserAccountRepository users, PasswordEncoder passwordEncoder,
                                  AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = UserAccountRules.normalizeEmail(request.email());
        if (!UserAccountRules.isValidPassword(request.password())) {
            throw new IllegalArgumentException("Password must be at least 8 characters and include upper, lower, and numeric characters");
        }
        if (users.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        UserAccount account = new UserAccount(email, passwordEncoder.encode(request.password()), request.displayName());
        users.save(account);
        return new AuthDtos.AuthResponse(jwtService.generate(email), account.getEmail(), account.getDisplayName());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        UserAccountRules.normalizeEmail(request.email()), request.password()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        UserAccount account = users.findByEmail(user.getUsername()).orElseThrow();
        return new AuthDtos.AuthResponse(jwtService.generate(user.getUsername()),
                account.getEmail(), account.getDisplayName());
    }
}
