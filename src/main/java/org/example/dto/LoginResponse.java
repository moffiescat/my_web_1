package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String message;
    private String username;
    private String email;

    public static LoginResponse success(String username, String email) {
        return new LoginResponse("登录成功", username, email);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(message, null, null);
    }
}
