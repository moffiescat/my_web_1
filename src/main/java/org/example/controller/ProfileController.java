package org.example.controller;

import org.example.model.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人信息控制器
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    // 从配置文件中读取默认个人信息
    @Value("${app.default-profile.name}")
    private String defaultName;
    
    @Value("${app.default-profile.age}")
    private int defaultAge;
    
    @Value("${app.default-profile.profession}")
    private String defaultProfession;
    
    @Value("${app.default-profile.email}")
    private String defaultEmail;

    /**
     * 获取个人信息
     * @return 个人信息对象
     */
    @GetMapping("/profile")
    public Profile getProfile() {
        return new Profile(defaultName, defaultAge, defaultProfession, defaultEmail);
    }
}
