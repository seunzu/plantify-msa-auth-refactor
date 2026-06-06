package com.plantify.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/api/demo/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "userId", authentication.getPrincipal(),
                "authorities", authentication.getAuthorities()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
