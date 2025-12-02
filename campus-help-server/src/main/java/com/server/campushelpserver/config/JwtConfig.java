package com.server.campushelpserver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtConfig {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private Long expiration;

    @Value("${security.jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${security.jwt.token-prefix}")
    private String tokenPrefix;

    @Value("${security.jwt.header}")
    private String header;

    /**
     * 获取密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新 Token
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 Token 中获取 Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 Token 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("email");
    }
    
    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return ((Number) claims.get("userId")).longValue();
    }

    /**
     * 获取 Token 前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * 获取请求头名称
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取过期时间
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * 获取刷新过期时间
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}

