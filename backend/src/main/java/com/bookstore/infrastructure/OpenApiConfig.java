package com.bookstore.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI bookstoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Simple Online Bookstore API")
                        .version("0.1.0")
                        .description("""
                                REST API for the bookstore catalogue, authentication, and authenticated cart.
                                The catalogue supports title search and offset pagination through
                                GET /api/v1/books?offset=0&limit=5&search=clean.
                                Checkout, payment, and order processing are intentionally not available yet.
                                """)
                        .contact(new Contact().name("Bookstore Development Team")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT returned by POST /api/auth/login or POST /api/auth/register"))
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Compatibility authentication supported by the backend")));
    }
}
