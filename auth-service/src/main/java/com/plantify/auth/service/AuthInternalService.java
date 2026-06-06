package com.plantify.auth.service;

import com.plantify.auth.domain.dto.response.KakaoInfoResponse;
import com.plantify.auth.domain.entity.User;

public interface AuthInternalService {

    User findOrCreateMember(KakaoInfoResponse response);
    String resolveAccessToken(String authorizationHeader);
}
