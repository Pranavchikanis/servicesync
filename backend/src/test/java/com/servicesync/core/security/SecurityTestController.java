package com.servicesync.core.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

    @GetMapping("/api/v1/public/tickets/test")
    public String publicEndpoint() {
        return "public";
    }

    @GetMapping("/api/v1/protected-test-dummy")
    public String protectedEndpoint() {
        return "protected";
    }
}
