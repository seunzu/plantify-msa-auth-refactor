package com.plantify.auth.service;

import com.plantify.auth.global.exception.ApplicationException;
import com.plantify.auth.global.exception.errorcode.AuthErrorCode;
import com.plantify.auth.jwt.JwtAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtAuthProvider jwtAuthProvider;

    @Override
    public String createAccessToken(Long userId) {
        return jwtAuthProvider.createAccessToken(userId);
    }

    @Override
    public String createRefreshToken(Long userId) {
        return jwtAuthProvider.createRefreshToken(userId);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        if (token == null || !jwtAuthProvider.validateToken(token)) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
        return jwtAuthProvider.getClaims(token).get("userId", Long.class);
    }
}
