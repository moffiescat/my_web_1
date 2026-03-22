package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ApiResponse;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.RegisterRequest;
import org.example.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}, email={}", request.getUsername(), request.getEmail());
        try {
            authService.register(request);
            log.info("注册成功: username={}", request.getUsername());
            return ApiResponse.success("注册成功", null);
        } catch (Exception e) {
            log.error("注册失败: username={}, error={}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: username={}", request.getUsername());
        try {
            LoginResponse response = authService.login(request);
            log.info("登录成功: username={}", request.getUsername());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("登录失败: username={}, error={}", request.getUsername(), e.getMessage());
            throw e;
        }
    }
}
