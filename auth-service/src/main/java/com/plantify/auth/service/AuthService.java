package com.plantify.auth.service;

import com.plantify.auth.domain.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(String authorizationCode);
    String refreshAccessToken(String authorizationHeader);
    String createAccessTokenForExperiment(Long userId);
    Long getUserId(String username);
}
