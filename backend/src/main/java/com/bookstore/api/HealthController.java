package com.bookstore.api;

import org.springframework.web.bind.annotation.*;
import com.bookstore.infrastructure.ApiRoutes;
import java.util.Map;

@RestController
@RequestMapping(ApiRoutes.VERSIONED_API)
public class HealthController {
    @GetMapping(ApiRoutes.HEALTH_PATH)
    public Map<String, String> health() { return Map.of("status", "UP", "service", "bookstore-backend"); }
}
