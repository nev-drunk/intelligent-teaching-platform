package com.huadi.intelligentteachingplatform.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:IntelligentTeachingPlatform2024SecretKey}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private int expireHours;

    /**
     * 生成Token
     * @param teacherId 教师ID
     * @param username 用户名
     * @param name 教师姓名
     * @param rememberMe 是否记住我（7天有效期）
     */
    public String generateToken(Long teacherId, String username, String name, boolean rememberMe) {
        long expireMillis = rememberMe 
            ? 7 * 24 * 60 * 60 * 1000L  // 7天
            : expireHours * 60 * 60 * 1000L;  // 默认24小时

        Map<String, Object> claims = new HashMap<>();
        claims.put("teacherId", teacherId);
        claims.put("username", username);
        claims.put("name", name);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        JwtBuilder builder = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMillis))
                .signWith(key);

        return builder.compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        return claims != null && claims.getExpiration().after(new Date());
    }

    /**
     * 从Token中获取教师ID
     */
    public Long getTeacherId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("teacherId", Long.class) : null;
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从Token中获取教师姓名
     */
    public String getName(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("name", String.class) : null;
    }
}
