package com.plantify.auth.domain.dto.response;

import com.plantify.auth.domain.entity.User;

public record UserResponse(Long userId, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getRole().toString()
        );
    }
}
