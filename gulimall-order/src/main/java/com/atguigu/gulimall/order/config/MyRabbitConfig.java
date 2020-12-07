package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("Close expired order:" + entity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * customize rabbitTemplate
     * 1. Broker receive message, invoke callback
     *      a. spring.rabbitmq.publisher-confirm-type=correlated
     *      b. set up ConfirmCallback
     * 2. Queue reveive message, invoke callback
     *      a. spring.rabbitmq.publisher-returns=true
     *          spring.rabbitmq.template.mandatory=true
     *      b. set up ReturnCallback
     * 3. Consumer confirm (message is consumed, broker can delete the message)
     *      a. default: auto confirm. broker delete the message
     *          Problem:
     *              Receive many message, auto return to broker, but only one success.
     *      b. set up manuall mode
     *          spring.rabbitmq.listener.simple.acknowledge-mode=manual
     *      c. manual ack
     *          channel.basicAck(deliveryTag, false);
     *          channel.basicNack(deliveryTag, false, false);
     */
    @PostConstruct // MyRabbitConfig created, call this method
    public void initRabbitTemplate() {
        RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData
             * @param ack
             * @param cause
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm..." + correlationData + " " + ack + " " + cause);
            }
        };
        rabbitTemplate.setConfirmCallback(confirmCallback);

        RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
            /**
             *
             * @param message
             * @param replyCode
             * @param replyText
             * @param exchange
             * @param routingKey
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

                System.out.println("Failed msg: " + message + " replyCode " + replyCode );
            }
        };
        rabbitTemplate.setReturnCallback(returnCallback);
    }

}
