package com.atguigu.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session核心原理
 * 1）@EnableRedisHttpSession导入RedisHttpSessionConfiguration
 *      1。 给容器中添加了一个组件
 *          SessionRepository ->>> RedisIndexedSessionRepository ->>> redis操作session。 session的增删改查封装类
 *      2。 SessionRepositoryFilter：Session存储过滤器；每个请求过来都经过过滤
 *          1) 创建的时候，自动获取sessionRepository
 *          2）原始的request，response都被包装： SessionRepositoryRequestWrapper SessionRepositoryResponseWrapper
 *          3）以后获取session。 request.getSession();
 *          4) wrappedRequest.getSession(); ->>> SessionRepository中获取的
 *
 *      自动续期，设有过期时间
 *
 */

@EnableRedisHttpSession // Integrate redis to store session
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthApplication.class, args);
    }

}

