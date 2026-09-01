package com.plantify.auth.service;

import com.plantify.auth.domain.entity.Role;
import com.plantify.auth.global.exception.ApplicationException;
import com.plantify.auth.global.exception.errorcode.AuthErrorCode;
import com.plantify.auth.jwt.JwtAuthProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final String REFRESH_TOKEN_SUBJECT = "Refresh";

    private final JwtAuthProvider jwtAuthProvider;

    @Override
    public String createAccessToken(Long userId, Role role) {
        return jwtAuthProvider.createAccessToken(userId, role);
    }

    @Override
    public String createRefreshToken(Long userId) {
        return jwtAuthProvider.createRefreshToken(userId);
    }

    @Override
    public Long getUserIdFromRefreshToken(String token) {
        Claims claims = getValidClaims(token);
        if (!REFRESH_TOKEN_SUBJECT.equals(claims.getSubject())) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
        return claims.get("userId", Long.class);
    }

    private Claims getValidClaims(String token) {
        if (token == null) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
        return jwtAuthProvider.getClaims(token);
    }
}
