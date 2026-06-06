package com.plantify.auth.repository;

import com.plantify.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByUsername(String username);

}
