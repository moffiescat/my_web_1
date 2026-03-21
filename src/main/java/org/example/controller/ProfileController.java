package org.example.controller;

import org.example.model.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/profile")
    public Profile getProfile() {
        return new Profile("谢沁桐", 20, "开发者", "2902125312@qq.com");
    }
}
