package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelcomeMailMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private LocalDateTime registeredAt;
}
