package com.bookstore.api;

import com.bookstore.application.AuthApplicationService;
import com.bookstore.dto.AuthDtos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test
    void delegatesRegistrationAndLoginRequests() {
        AuthApplicationService service = mock(AuthApplicationService.class);
        AuthController controller = new AuthController(service);
        AuthDtos.RegisterRequest registration = new AuthDtos.RegisterRequest("reader@example.com", "password", "Reader");
        AuthDtos.LoginRequest login = new AuthDtos.LoginRequest("reader@example.com", "password");
        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse("jwt", "reader@example.com", "Reader");
        when(service.register(registration)).thenReturn(response);
        when(service.login(login)).thenReturn(response);

        assertSame(response, controller.register(registration));
        assertSame(response, controller.login(login));
    }
}
