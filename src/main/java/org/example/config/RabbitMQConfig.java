package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConfig {

    public static final String MAIL_EXCHANGE = "mail.exchange";
    public static final String MAIL_QUEUE = "mail.welcome.queue";
    public static final String MAIL_ROUTING_KEY = "mail.welcome";
    public static final String MAIL_DLX_EXCHANGE = "mail.dlx.exchange";
    public static final String MAIL_DLQ = "mail.welcome.dlq";
    public static final String MAIL_DLX_ROUTING_KEY = "mail.welcome.dead";

    private final ConnectionFactory connectionFactory;

    // 1. 定义死信队列
    @Bean
    public Queue mailDeadLetterQueue() {
        return QueueBuilder.durable(MAIL_DLQ).build();
    }

    // 2. 定义死信交换机
    @Bean
    public DirectExchange mailDeadLetterExchange() {
        return new DirectExchange(MAIL_DLX_EXCHANGE);
    }

    // 3. 绑定死信队列到死信交换机
    @Bean
    public Binding mailDlxBinding() {
        return BindingBuilder
                .bind(mailDeadLetterQueue())
                .to(mailDeadLetterExchange())
                .with(MAIL_DLX_ROUTING_KEY);
    }

    // 4. 定义邮件队列 (配置死信交换机)
    @Bean
    public Queue mailWelcomeQueue() {
        return QueueBuilder.durable(MAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", MAIL_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MAIL_DLX_ROUTING_KEY)
                .build();
    }

    // 5. 定义邮件交换机
    @Bean
    public DirectExchange mailExchange() {
        return new DirectExchange(MAIL_EXCHANGE);
    }

    // 6. 绑定队列到交换机
    @Bean
    public Binding mailBinding() {
        return BindingBuilder
                .bind(mailWelcomeQueue())
                .to(mailExchange())
                .with(MAIL_ROUTING_KEY);
    }

    // 7. JSON 消息转换器
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 8. 配置 RabbitTemplate (开启确认和返回)
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());

        // 消息确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送失败: correlationData={}, cause={}",
                        correlationData, cause);
            } else {
                log.info("消息发送成功: correlationData={}", correlationData);
            }
        });

        // 消息返回回调 (路由不到队列时触发)
        template.setReturnsCallback(returned -> {
            log.error("消息路由失败: exchange={}, routingKey={}, message={}, replyCode={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText());
        });

        return template;
    }
}
