package com.plantify.auth.service;

import com.plantify.auth.domain.dto.response.LoginResponse;
import com.plantify.auth.domain.dto.response.UserResponse;

public interface AuthService {

    LoginResponse login(String authorizationCode);
    String refreshAccessToken(String authorizationHeader);
    UserResponse getUserIdAndRoleFromToken(String authorizationHeader);
    String createAccessTokenForExperiment(Long userId);
    Long getUserId(String username);
}
