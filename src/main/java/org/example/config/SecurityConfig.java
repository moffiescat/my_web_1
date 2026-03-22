package org.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * 安全配置类 - 配置 Spring Security 相关设置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 注入 JWT 认证过滤器
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    // 从配置文件中读取安全配置
    @Value("${security.csrf.enabled}")
    private boolean csrfEnabled;
    
    @Value("${security.allowed-paths}")
    private List<String> allowedPaths;

    /**
     * 密码编码器 Bean - 使用 BCrypt 加密
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     * @param http HttpSecurity
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> {
                if (!csrfEnabled) {
                    csrf.disable();
                }
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // 配置允许匿名访问的路径
                for (String path : allowedPaths) {
                    auth.requestMatchers(path).permitAll();
                }
                // 其他所有请求需要认证
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
