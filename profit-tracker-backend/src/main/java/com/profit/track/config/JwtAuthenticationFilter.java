package com.profit.track.config;

import com.profit.track.service.SysPermissionService;
import com.profit.track.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

/** JWT 认证过滤器 - RBAC 改造版 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SysPermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String token = extractToken(request);

        if (token == null) {
            // 没有 Token，交由 Spring Security 处理（返回 401）
            logger.debug("[JWT Filter] No token for: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                // Token 无效或已过期，交由 Spring Security 异常处理
                logger.warn("[JWT Filter] Invalid token for: " + requestURI);
                filterChain.doFilter(request, response);
                return;
            }

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

            // 构建权限标识列表
            List<SimpleGrantedAuthority> authorities = buildAuthorities(permissions, roles);

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

            logger.debug("[JWT Filter] Authenticated user: " + username + " (perms: " + permissions.size() + ")");

        } catch (Exception e) {
            logger.error("[JWT Filter] Error processing token for " + requestURI + ": " + e.getMessage());
            // 异常时继续过滤器链，交由 Spring Security 处理
        }

        // 继续过滤器链
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

    /** 构建权限列表，避免双重 ROLE_ 前缀 */
    private List<SimpleGrantedAuthority> buildAuthorities(List<String> permissions, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

        // 添加权限
        for (String perm : permissions) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }

        // 添加角色（Spring Security 要求角色带 ROLE_ 前缀）
        for (String role : roles) {
            if (!role.startsWith("ROLE_")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            } else {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }

        return authorities;
    }
}
