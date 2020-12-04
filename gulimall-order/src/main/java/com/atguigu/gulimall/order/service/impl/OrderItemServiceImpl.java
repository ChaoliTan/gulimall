package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     *
     * org.springframework.amqp.core.Message
     *
     *  1. Message message
     *  2. T<The type when message sent>
     *  3. Channel channel
     *
     *
     *  Queue: Many people can listen; When message be received, queue delete message,
     *          only one can receive.
     *          1) One message, one clint receive
     *          2) One message processed, can receive next message
     * @param msg
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message msg,
                               OrderReturnReasonEntity content,
                               Channel channel)  {
//        System.out.println("Received message: " + msg + ", content: " + content);

        System.out.println("Processed message: " + content.getName());


        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag:" + deliveryTag);
        try {
//            channel.basicAck(deliveryTag, false);
            // long deliveryTag, boolean multiple, boolean requeue
            channel.basicNack(deliveryTag, false, false);
            // long deliveryTag, boolean requeue
//            channel.basicReject(deliveryTag, false);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity content)  {
        System.out.println("Processed message: " + content);
    }

}