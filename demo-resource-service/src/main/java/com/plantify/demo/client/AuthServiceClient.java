package com.plantify.demo.client;

import com.plantify.demo.dto.ApiResponse;
import com.plantify.demo.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.validate-token-url}")
    private String baseUrl;

    public UserInfo validateToken(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                baseUrl + "/v1/auth/validate-token",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<UserInfo>>() {}
        ).getBody().getData();
    }
}
