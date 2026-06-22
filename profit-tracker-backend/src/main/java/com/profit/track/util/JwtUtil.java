package com.profit.track.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mhxy-secret-key-for-jwt-token-generation-must-be-at-least-256-bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 Token */
    public String generateToken(Long userId, String username, String roleName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleName", roleName);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 生成 Token（含权限和角色列表） */
    public String generateToken(Long userId, String username, List<String> permissions, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("permissions", permissions);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 从 Token 中获取 Claims */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 从 Token 中获取 userId */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /** 从 Token 中获取 username */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /** 从 Token 中获取 roleName */
    public String getRoleNameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roleName", String.class);
    }

    /** 从 Token 中获取权限列表 */
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = parseToken(token);
        Object perms = claims.get("permissions");
        if (perms instanceof List) {
            return ((List<?>) perms).stream().map(Object::toString).collect(Collectors.toList());
        }
        return new java.util.ArrayList<>();
    }

    /** 从 Token 中获取角色列表 */
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return ((List<?>) roles).stream().map(Object::toString).collect(Collectors.toList());
        }
        // 兼容旧 Token：从 roleName 字段回退
        String roleName = getRoleNameFromToken(token);
        if (roleName != null && !roleName.isEmpty()) {
            return java.util.Collections.singletonList(roleName);
        }
        return new java.util.ArrayList<>();
    }

    /** 判断 Token 是否包含权限列表（用于检测旧 Token） */
    public boolean tokenHasPermissions(String token) {
        Claims claims = parseToken(token);
        return claims.get("permissions") != null;
    }

    /** 验证 Token 是否过期 */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
