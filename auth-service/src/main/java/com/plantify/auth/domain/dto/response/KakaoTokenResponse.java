package com.plantify.auth.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") String expiresIn,
        @JsonProperty("refresh_token_expires_in") String refreshTokenExpiresIn) {
}
