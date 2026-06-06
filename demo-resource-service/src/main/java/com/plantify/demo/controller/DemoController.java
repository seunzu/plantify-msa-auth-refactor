package com.plantify.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/api/demo/me")
    public Map<String, Object> me(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Map.of(
                "userId", jwt.getClaim("userId"),
                "authorities", authentication.getAuthorities()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
