package com.profit.track.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profit.track.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    /** 密码编码器 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 认证管理器 */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** 用户详情服务（Security 需要，但本系统使用 JWT 自定义鉴权） */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Security UserDetailsService 已禁用，使用自定义 JWT 鉴权");
        };
    }

    /** 安全过滤链配置 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（JWT 无状态，不需要 CSRF 保护）
                .csrf().disable()
                // 配置 CORS
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOriginPattern("*");
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    config.addAllowedHeader("*");
                    config.addAllowedMethod("*");
                    config.addExposedHeader("Authorization");
                    config.addExposedHeader("X-Token-Refreshed");
                    return config;
                }))
                // 禁用 Session 管理（JWT 无状态）
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // ===== 公开接口：无需认证 =====
                        .antMatchers("/api/auth/login", "/api/auth/register", "/api/auth/phone-login",
                                "/api/auth/reset-password", "/api/auth/send-code",
                                "/api/auth/send-login-code",
                                "/api/auth/send-register-code", "/api/auth/send-reset-code",
                                "/api/auth/permissions").permitAll()
                        // Swagger 页面
                        .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()

                        // ===== 管理员接口兜底：需要 ADMIN 角色 =====
                        // 注意：Controller 上已有 @PreAuthorize 注解，此处为安全网
                        .antMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers("/api/roles/**").hasRole("ADMIN")

                        // 默认需要认证（细粒度权限由 @PreAuthorize 控制）
                        .anyRequest().authenticated()
                )
                // 将 JWT 过滤器插入到 AuthorizationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class)
                // 自定义 401/403 错误响应
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            Result<Void> result = Result.fail(401, "未登录或 Token 已过期");
                            objectMapper.writeValue(response.getOutputStream(), result);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            Result<Void> result = Result.fail(403, "没有权限访问该资源");
                            objectMapper.writeValue(response.getOutputStream(), result);
                        })
                );

        return http.build();
    }
}
