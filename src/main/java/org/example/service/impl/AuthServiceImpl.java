package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserDao;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.RegisterRequest;
import org.example.dto.WelcomeMailMessage;
import org.example.entity.User;
import org.example.exception.BusinessException;
import org.example.producer.MailProducer;
import org.example.service.AuthService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MailProducer mailProducer;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public void register(RegisterRequest request) {
        log.info("开始注册用户: username={}", request.getUsername());
        if (userDao.existsByUsername(request.getUsername())) {
            log.warn("用户名已存在: username={}", request.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setCreatedAt(LocalDateTime.now());

        userDao.save(user);
        log.info("用户注册成功: username={}, id={}", request.getUsername(), user.getId());

        // 异步发送欢迎邮件
        WelcomeMailMessage mailMessage = WelcomeMailMessage.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .registeredAt(user.getCreatedAt())
                .build();

        mailProducer.sendWelcomeMail(mailMessage);
        log.info("欢迎邮件消息已发布: username={}", request.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("开始登录: username={}", request.getUsername());
        User user = userDao.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("用户不存在: username={}", request.getUsername());
                    return new BusinessException(401, "用户名或密码错误");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("密码错误: username={}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        log.info("登录成功: username={}, token生成成功, 过期时间={}ms", request.getUsername(), expiration);
        return LoginResponse.success(user.getUsername(), user.getEmail(), token);
    }
}
