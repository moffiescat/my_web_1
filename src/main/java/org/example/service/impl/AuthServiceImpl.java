package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dao.UserDao;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.RegisterRequest;
import org.example.entity.User;
import org.example.exception.BusinessException;
import org.example.service.AuthService;
import org.example.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void register(RegisterRequest request) {
        if (userDao.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        userDao.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return LoginResponse.success(user.getUsername(), user.getEmail(), token);
    }
}
