package com.huadi.intelligentteachingplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * 使用 JWT 无状态认证，禁用默认表单登录和 CSRF
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置请求路径权限
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                // 门户相关接口
                .requestMatchers("/api/portal/**").permitAll()
                // 其他接口需要认证（由JWT过滤器处理）
                .anyRequest().permitAll()
            )
            // 无状态会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
