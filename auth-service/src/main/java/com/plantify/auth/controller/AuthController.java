package com.plantify.auth.controller;

import com.plantify.auth.domain.dto.response.LoginResponse;
import com.plantify.auth.domain.dto.response.UserResponse;
import com.plantify.auth.global.response.ApiResponse;
import com.plantify.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestParam("code") String authorizationCode) {
        LoginResponse loginResponse = authService.login(authorizationCode);
        return ApiResponse.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refreshAccessToken(@RequestHeader("Authorization") String authorizationHeader) {
        String newAccessToken = authService.refreshAccessToken(authorizationHeader);
        return ApiResponse.ok(newAccessToken);
    }

    @PostMapping("/validate-token")
    public ApiResponse<UserResponse> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        UserResponse userInfo = authService.getUserIdAndRoleFromToken(authorizationHeader);
        return ApiResponse.ok(userInfo);
    }

    @PostMapping("/dev-token")
    public ApiResponse<String> createLocalExperimentToken() {
        return ApiResponse.ok(authService.createAccessTokenForExperiment(1L));
    }

    @GetMapping("/users/search")
    public ApiResponse<Long> getUserId(@RequestParam String username) {
        Long userId = authService.getUserId(username);
        return ApiResponse.ok(userId);
    }
}
