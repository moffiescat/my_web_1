package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.ApiResponse;
import org.example.model.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class ProfileController {

    @Value("${app.default-profile.name}")
    private String defaultName;
    
    @Value("${app.default-profile.age}")
    private int defaultAge;
    
    @Value("${app.default-profile.profession}")
    private String defaultProfession;
    
    @Value("${app.default-profile.email}")
    private String defaultEmail;

    @GetMapping("/profile")
    public ApiResponse<Profile> getProfile() {
        log.info("获取个人信息");
        try {
            Profile profile = new Profile(defaultName, defaultAge, defaultProfession, defaultEmail);
            log.info("个人信息获取成功: name={}", defaultName);
            return ApiResponse.success(profile);
        } catch (Exception e) {
            log.error("获取个人信息失败: error={}", e.getMessage());
            throw e;
        }
    }
}
