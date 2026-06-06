package com.plantify.auth.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoInfoResponse(Long id, @JsonProperty("properties") KakaoProperties properties) {

    public record KakaoProperties(String nickname) {}

    public String getUsername() {
        return properties.nickname();
    }
}
