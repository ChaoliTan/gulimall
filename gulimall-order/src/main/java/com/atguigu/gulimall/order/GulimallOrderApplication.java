package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

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
 *
 * Local Transaction Disable Problem
 * 同一个对象内事务方法互调默认失效，原因 绕过了代理对象
 * Solution:
 *  1) 引入aop-starter: spring-boot-starter-aop； 引入了aspectj
 *  2）@EnableAspectJAutoProxy(exposeProxy = true): 开启aspectj动态代理功能
 *  3) 本类互调用 调用对象
 *      OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
 *
 *
 * Seata控制分布式事务
 *  1）为每一个数据库创建undo_log table
 *  2) 安装Seata-Server: https://github.com/seata/seata/releases
 *  3) Integrate
 *      1. Add dependency in gulimall-common： seata-all-1.3.0
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
 *         </dependency>
 *      2。Start seata-server
 *          registry.conf: registry type = "nacos"
 *      3. Use seata DataSourceProxy proxy datasource
 *      4. Every sevice should include
 *          file.conf vgroup_mapping.${spring.application.name}-fescar-service-group
 *          registry.conf
 *      5. entry @GlobalTransactional
 *      6. remote @Transactional
 *
 *
 */

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = "com.atguigu.gulimall.order.feign")
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication // (exclude = GlobalTransactionAutoConfiguration.class)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
