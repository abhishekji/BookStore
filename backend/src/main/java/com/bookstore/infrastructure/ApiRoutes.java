package com.bookstore.infrastructure;

public final class ApiRoutes {
    public static final String VERSIONED_API = "/api/v1";
    public static final String HEALTH_PATH = "/health";
    public static final String ALL_PATHS = "/**";
    public static final String BOOKS = "/api/v1/books";
    public static final String HEALTH = "/api/v1/health";
    public static final String AUTH = "/api/auth";
    public static final String CART = "/api/cart";
    public static final String OPENAPI = "/v3/api-docs";
    public static final String SWAGGER_UI = "/swagger-ui";

    private ApiRoutes() {
    }
}
