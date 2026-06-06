package com.plantify.auth.domain.dto.response;

import com.plantify.auth.domain.entity.User;

public record LoginResponse(Long id, String username, String accessToken, String refreshToken) {

    public static LoginResponse from(User user, String accessToken, String refreshToken) {
        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                accessToken,
                refreshToken
        );
    }
}
