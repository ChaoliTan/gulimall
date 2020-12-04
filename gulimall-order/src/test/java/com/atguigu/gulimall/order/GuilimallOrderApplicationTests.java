package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);

        reasonEntity.setCreateTime(new Date());
        // 1. send message, if message is object, will use serializer. This object must implement interface Serializable.
        String msg = "Hello World!";

        // 2. message can be sent as json format.
        for (int i = 0; i < 10; ++i) {
            if (i % 2 == 0) {
                reasonEntity.setName("Hug" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
                log.info("Message [{}] sent success.", reasonEntity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn("uber" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity, new CorrelationData(UUID.randomUUID().toString()));
                log.info("Message [{}] sent success.", orderEntity);
            }
        }

    }


    /**
     * 1. Create Exchange, Queue, Binding
     *      1) Using AmqpAdmin to create
     * 2. Publish/Subscribe message
     */
    @Test
    public void createExchange() {
        /**
         * DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}] created success.", "hello-java-exchange");
    }

    @Test
    public void createQueue() {
        /**
         * Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
         */
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("QUeue[{}] created success.", "hello-java-queue");
    }

    @Test
    public void createBinding() {
        /**
         * (String destination, DestinationType destinationType, String exchange, String routingKey,
         *                        @Nullable Map<String, Object> arguments)
         */
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,
                                      "hello-java-exchange",
                                      "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}] created success.", "hello-java-binding");

    }

}
