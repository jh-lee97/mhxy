package com.profit.track.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profit.track.dto.Result;
import com.profit.track.entity.SysPermission;
import com.profit.track.service.SysPermissionService;
import com.profit.track.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** JWT 认证过滤器 - RBAC 改造版 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final SysPermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 白名单路径：跳过 JWT 认证检查，直接放行
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    // 从 Token 中提取用户信息
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);

                    // 兼容旧 Token：如果 Token 不含权限，则从数据库加载并签发新 Token
                    boolean needsRefresh = !jwtUtil.tokenHasPermissions(token);
                    
                    List<String> permissions;
                    List<String> roles;
                    String newToken = token;

                    if (needsRefresh) {
                        // 从数据库加载权限
                        permissions = permissionService.getUserPermissions(userId);
                        roles = permissionService.getUserRoleCodes(userId);
                        
                        // 签发包含权限的新 Token
                        newToken = jwtUtil.generateToken(userId, username, permissions, roles);
                        
                        // 在响应头中返回新 Token（前端会自动使用）
                        response.setHeader("Authorization", "Bearer " + newToken);
                        response.setHeader("X-Token-Refreshed", "true");
                    } else {
                        // 新 Token：直接从 Token 中读取
                        permissions = jwtUtil.getPermissionsFromToken(token);
                        roles = jwtUtil.getRolesFromToken(token);
                    }

                    // 构建权限标识列表（ROLE_ 前缀用于 Spring Security）
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    
                    // 也添加角色权限
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    // 创建 Authentication 对象并设置到 SecurityContext
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );
                    authentication.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 同时将信息放入 request attribute 供业务代码使用
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    request.setAttribute("permissions", permissions);
                    request.setAttribute("roles", roles);
                    request.setAttribute("authorityList", authorities);

                    logger.info("[JWT Filter] Processed request for user: " + username + " (userId: " + userId + ", perms: " + permissions.size() + ")");
                } else {
                    // Token 无效或已过期
                    logger.warn("[JWT Filter] Invalid or expired token for request: " + requestURI);
                    sendUnauthorizedResponse(response, "Token 已过期或无效，请重新登录", 401);
                    return;
                }
            } catch (Exception e) {
                logger.error("[JWT Filter] Error processing token for request " + requestURI + ": " + e.getMessage());
                sendUnauthorizedResponse(response, "Token 解析失败: " + e.getMessage(), 401);
                return;
            }
        } else {
            // 没有 Token
            sendUnauthorizedResponse(response, "未登录或 Token 已过期", 401);
            return;
        }

        // Token 验证通过，继续过滤器链
        filterChain.doFilter(request, response);
    }

    /** 从请求头中获取 Token */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /** 判断是否为公开路径（无需 Token） */
    private boolean isPublicPath(String uri) {
        return uri.equals("/api/auth/login")
                || uri.equals("/api/auth/register")
                || uri.equals("/api/auth/phone-login")
                || uri.equals("/api/auth/reset-password")
                || uri.equals("/api/auth/send-code")
                || uri.equals("/api/auth/send-login-code")
                || uri.equals("/api/auth/send-register-code")
                || uri.equals("/api/auth/send-reset-code")
                || uri.equals("/api/auth/permissions")
                || uri.equals("/swagger-ui.html")
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs/")
                || uri.startsWith("/swagger-resources/")
                || uri.startsWith("/webjars/");
    }

    /** 发送未授权响应 */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        Result<Void> result = Result.fail(statusCode, message);
        objectMapper.writeValue(response.getOutputStream(), result);
    }
}
