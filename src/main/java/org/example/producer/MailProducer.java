package org.example.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.dto.WelcomeMailMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendWelcomeMail(WelcomeMailMessage message) {
        log.info("发送欢迎邮件消息: username={}, email={}",
                message.getUsername(), message.getEmail());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAIL_EXCHANGE,
                RabbitMQConfig.MAIL_ROUTING_KEY,
                message
        );

        log.info("欢迎邮件消息已投递: username={}", message.getUsername());
    }
}
