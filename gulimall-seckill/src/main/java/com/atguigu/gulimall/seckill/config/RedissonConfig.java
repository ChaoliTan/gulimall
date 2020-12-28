package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(@Value("${spring.redis.host}") String url){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + url + ":6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
