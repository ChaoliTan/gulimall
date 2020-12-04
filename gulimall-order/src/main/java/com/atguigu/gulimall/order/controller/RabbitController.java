package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class RabbitController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num", defaultValue = "10") Integer num) {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);

        reasonEntity.setCreateTime(new Date());
        // 1. send message, if message is object, will use serializer. This object must implement interface Serializable.
        String msg = "Hello World!";

        // 2. message can be sent as json format.
        for (int i = 0; i < num; ++i) {
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

        return "ok";
    }

}
