package com.atguigu.gulimall.warehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.atguigu.gulimall.warehouse.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWarehouseApplication.class, args);
    }

}
