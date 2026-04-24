package org.example.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.dto.WelcomeMailMessage;
import org.example.service.MailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailConsumer {

    private final MailService mailService;

    @RabbitListener(queues = RabbitMQConfig.MAIL_QUEUE)
    public void handleWelcomeMail(WelcomeMailMessage message,
                                   @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag,
                                   Channel channel) {

        log.info("收到欢迎邮件消息: username={}, email={}",
                message.getUsername(), message.getEmail());

        try {
            mailService.sendWelcomeEmail(message);
            // 手动 ACK
            channel.basicAck(deliveryTag, false);
            log.info("邮件处理成功，确认消息: deliveryTag={}", deliveryTag);

        } catch (Exception e) {
            log.error("邮件处理失败: username={}, error={}",
                    message.getUsername(), e.getMessage());

            try {
                // 拒绝消息，并重新入队
                channel.basicNack(deliveryTag, false, true);
                log.warn("消息已拒绝并重新入队: deliveryTag={}", deliveryTag);
            } catch (Exception ioException) {
                log.error("消息确认失败: deliveryTag={}", deliveryTag, ioException);
            }
        }
    }
}
