package org.example.controller;

import org.example.dto.ApiResponse;
import org.example.model.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
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
        Profile profile = new Profile(defaultName, defaultAge, defaultProfession, defaultEmail);
        return ApiResponse.success(profile);
    }
}
