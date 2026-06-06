package com.plantify.auth.jwt;

import com.plantify.auth.global.exception.ApplicationException;
import com.plantify.auth.global.exception.errorcode.AuthErrorCode;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createAccessToken(long userId) {
        return createToken(userId, "Access", accessTokenExpiration);
    }

    public String createRefreshToken(long userId) {
        return createToken(userId, "Refresh", refreshTokenExpiration);
    }

    private String createToken(Long userId, String type, long tokenValidTime) {
        Claims claims = Jwts.claims().setSubject(type);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
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
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: " + e.getMessage(), e);
            return false;
        }
    }
}
