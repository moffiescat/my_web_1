package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.ApiResponse;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.RegisterRequest;
import org.example.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}
