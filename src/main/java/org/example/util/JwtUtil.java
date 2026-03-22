package org.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        log.info("生成JWT令牌: username={}, 过期时间={}ms", username, expiration);
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            String token = Jwts.builder()
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getKey())
                    .compact();
            log.info("JWT令牌生成成功: username={}", username);
            return token;
        } catch (Exception e) {
            log.error("JWT令牌生成失败: username={}, error={}", username, e.getMessage());
            throw e;
        }
    }

    public String getUsernameFromToken(String token) {
        log.debug("解析JWT令牌");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject();
            log.debug("JWT令牌解析成功: username={}", username);
            return username;
        } catch (Exception e) {
            log.error("JWT令牌解析失败: error={}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        log.debug("验证JWT令牌");
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);
            log.debug("JWT令牌验证成功");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT令牌验证失败: error={}", e.getMessage());
            return false;
        }
    }
}
