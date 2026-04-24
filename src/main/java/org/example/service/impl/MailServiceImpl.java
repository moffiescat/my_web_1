package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.WelcomeMailMessage;
import org.example.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String from;

    @Override
    public void sendWelcomeEmail(WelcomeMailMessage message) {
        log.info("发送欢迎邮件: to={}, username={}", message.getEmail(), message.getUsername());

        try {
            Context context = new Context();
            context.setVariable("username", message.getUsername());
            context.setVariable("registeredAt", message.getRegisteredAt());
            context.setVariable("email", message.getEmail());

            String htmlContent = templateEngine.process("welcome", context);

            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(message.getEmail());
            helper.setSubject("欢迎注册 - " + message.getUsername());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("欢迎邮件发送成功: to={}", message.getEmail());

        } catch (Exception e) {
            log.error("邮件发送失败: to={}, error={}", message.getEmail(), e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
