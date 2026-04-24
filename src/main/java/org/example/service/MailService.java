package org.example.service;

import org.example.dto.WelcomeMailMessage;

public interface MailService {
    void sendWelcomeEmail(WelcomeMailMessage message);
}
