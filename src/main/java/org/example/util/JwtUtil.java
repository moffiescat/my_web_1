package org.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 - 负责生成、解析和验证 JWT 令牌
 */
@Component
public class JwtUtil {

    // 从配置文件中读取密钥
    @Value("${jwt.secret}")
    private String secret;
    
    // 从配置文件中读取过期时间
    @Value("${jwt.expiration}")
    private long expiration;

    // 基于密钥生成 HMAC-SHA256 签名密钥
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT 令牌
     * @param username 用户名
     * @return JWT 令牌字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)  // 设置用户名作为主题
                .issuedAt(now)      // 签发时间
                .expiration(expiryDate)  // 过期时间
                .signWith(getKey())      // 使用密钥签名
                .compact();         // 生成最终的 JWT 字符串
    }

    /**
     * 从令牌中提取用户名
     * @param token JWT 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())     // 使用密钥验证签名
                .build()
                .parseSignedClaims(token)  // 解析令牌
                .getPayload();       // 获取载荷部分
        return claims.getSubject();  // 返回主题（用户名）
    }

    /**
     * 验证令牌的有效性
     * @param token JWT 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())     // 验证签名
                    .build()
                    .parseSignedClaims(token);  // 解析令牌
            return true;  // 验证通过
        } catch (JwtException | IllegalArgumentException e) {
            // 验证失败（签名错误或令牌过期）
            return false;
        }
    }
}
