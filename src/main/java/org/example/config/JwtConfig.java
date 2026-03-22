package org.example.config;

import org.example.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * JWT 相关配置类
 */
@Configuration
public class JwtConfig {

    /**
     * 配置 JWT 认证过滤器
     * @param jwtUtil JwtUtil
     * @param userDetailsService UserDetailsService
     * @return JwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.setJwtUtil(jwtUtil);
        filter.setUserDetailsService(userDetailsService);
        return filter;
    }
}
