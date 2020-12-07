package com.atguigu.gulimall.product.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Configuration
public class MyThreadConfig {

    @Bean
    public ThreadPoolExecutor executor(ThreadPoolConfigProperties threadPoolConfigProperties) {
        return new ThreadPoolExecutor(
                threadPoolConfigProperties.getCorePoolSize(),
                threadPoolConfigProperties.getMaxPoolSize(),
                threadPoolConfigProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
