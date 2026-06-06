package com.plantify.auth.config;

import com.plantify.auth.domain.entity.Role;
import com.plantify.auth.domain.entity.User;
import com.plantify.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LocalDataInitializer {

    private final UserRepository userRepository;

    @Bean
    CommandLineRunner seedLocalUser() {
        return args -> userRepository.findById(1L)
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(1001L)
                        .username("local-user")
                        .role(Role.USER)
                        .build()));
    }
}
