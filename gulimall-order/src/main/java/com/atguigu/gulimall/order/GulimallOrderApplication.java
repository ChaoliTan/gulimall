package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Use RabbitMQ
 * 1. Introduce RabbitMQ, RabbitAutoConfiguration will be active
 * 2. RabbitTemplate, AmqpAdmin, CachingConnectionFactory, RabbitMessagingTemplate
 *      @EnableConfigurationProperties(RabbitProperties.class)
 * 3. Config spring.rabbitmq
 * 4. @EnableRabbit
 * 5. Message listener, use @RabbitListener
 *      RabbitListener: class + method (Listen queues)
 *      RabbitHandler: method   (Overload different messages)
 */

@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
