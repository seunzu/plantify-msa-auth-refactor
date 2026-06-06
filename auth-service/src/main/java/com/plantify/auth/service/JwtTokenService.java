package com.plantify.auth.service;

import com.plantify.auth.domain.entity.Role;

public interface JwtTokenService {

    String createAccessToken(Long userId, Role role);
    String createRefreshToken(Long userId);
    Long getUserIdFromToken(String token);
}
