package com.plantify.auth.jwt;

import com.plantify.auth.config.RsaKeyProvider;
import com.plantify.auth.global.exception.ApplicationException;
import com.plantify.auth.global.exception.errorcode.AuthErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.plantify.auth.domain.entity.Role;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthProvider {

    private final RsaKeyProvider rsaKeyProvider;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String createAccessToken(long userId, Role role) {
        return createToken(userId, role, "Access", accessTokenExpiration);
    }

    public String createRefreshToken(long userId) {
        return createToken(userId, null, "Refresh", refreshTokenExpiration);
    }

    private String createToken(Long userId, Role role, String type, long tokenValidTime) {
        Claims claims = Jwts.claims().setSubject(type);
        claims.put("userId", userId);
        if (role != null) {
            claims.put("role", role.name());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .setHeaderParam("kid", rsaKeyProvider.getKeyId())
                .signWith(rsaKeyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(rsaKeyProvider.getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new ApplicationException(AuthErrorCode.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(AuthErrorCode.TOKEN_CLAIMS_EMPTY);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(rsaKeyProvider.getPublicKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: " + e.getMessage(), e);
            return false;
        }
    }
}
