package com.plantify.auth.service;

import com.plantify.auth.client.KakaoApiClient;
import com.plantify.auth.domain.dto.response.KakaoInfoResponse;
import com.plantify.auth.domain.dto.response.LoginResponse;
import com.plantify.auth.domain.dto.response.KakaoTokenResponse;
import com.plantify.auth.domain.dto.response.UserResponse;
import com.plantify.auth.domain.entity.Role;
import com.plantify.auth.domain.entity.User;
import com.plantify.auth.global.exception.ApplicationException;
import com.plantify.auth.global.exception.errorcode.AuthErrorCode;
import com.plantify.auth.global.exception.errorcode.UserErrorCode;
import com.plantify.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, AuthInternalService {

    private final JwtTokenService jwtTokenService;
    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(String authorizationCode) {
        KakaoTokenResponse tokenResponse = kakaoApiClient.requestAccessToken(authorizationCode);
        KakaoInfoResponse infoResponse = kakaoApiClient.requestKakaoUserInfo(tokenResponse.accessToken());

        User user = findOrCreateMember(infoResponse);
        String accessToken = jwtTokenService.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenService.createRefreshToken(user.getUserId());

        return LoginResponse.from(user, accessToken, refreshToken);
    }

    @Override
    public String refreshAccessToken(String authorizationHeader) {
        String token = resolveAccessToken(authorizationHeader);
        Long userId = jwtTokenService.getUserIdFromToken(token);
        return jwtTokenService.createAccessToken(userId);
    }

    @Override
    public UserResponse getUserIdAndRoleFromToken(String authorizationHeader) {
        String token = resolveAccessToken(authorizationHeader);
        Long userId = jwtTokenService.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    @Override
    public String createAccessTokenForExperiment(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCode.USER_NOT_FOUND));
        return jwtTokenService.createAccessToken(userId);
    }

    @Override
    public User findOrCreateMember(KakaoInfoResponse response) {
        String username = response.getUsername();
        if (username == null || username.isEmpty()) {
            throw new ApplicationException(UserErrorCode.INVALID_USERNAME);
        }

        return userRepository.findByKakaoId(response.id())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(response.id())
                            .username(username)
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Override
    public String resolveAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
        return authorizationHeader.substring(7).trim();
    }

    @Override
    public Long getUserId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(UserErrorCode.USER_NOT_FOUND));
        return user.getUserId();
    }
}
