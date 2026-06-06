package com.plantify.auth.service;

import com.plantify.auth.domain.dto.response.KakaoInfoResponse;
import com.plantify.auth.domain.entity.User;

public interface JwtTokenService {

    String createAccessToken(Long userId);
    String createRefreshToken(Long userId);
    Long getUserIdFromToken(String token);
}
