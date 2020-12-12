package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1。 整合Sentinel
 *  1) 引入依赖
 *  2）下载sentinel console
 *  3）配置sentinel console address
 *      spring.cloud.sentinel.transport.dashboard=localhost:8333
 *      spring.cloud.sentinel.transport.port=8719
 *  4)引入依赖
 *      <dependency>
 *             <groupId>org.hibernate</groupId>
 *             <artifactId>hibernate-validator</artifactId>
 *             <version>6.1.6.Final</version>
 *         </dependency>
 *  5）在控制台添加规则 【默认保存在应用内存中，重启失效】
 *
 * 2。每一个微服务导入actuator,配置management.endpoints.web.exposure.include=*
 *
 * 3. 自定义sentinel流控返回的数据
 *
 * 4。 Sentinel fuse downgrade
 *      1。 调用方熔断保护：
 *      1) feign.sentinel.enabled=true
 *      2) @FeignClient(value = "gulimall-seckill",fallback = SeckillFallbackService.class)
 *         public interface SeckillFeignService {
 *     2。 调用方手动指定降级：远程服务被降级处理，触发熔断回调方法
 *
 *     3。 服务提供方（远程服务）指定降级策略，返回的是默认的降级数据（限流的数据）
 *
 * 5. 自定义受保护的资源
 *      1. 代码
 *          try() {
 *
 *          } catch() {}
 *      2. 基于注解
 *          @SentinelResource(value = "name", blockHandler = "blockHandler", fallback = "", fallbackClass = "")
 *
 *
 *
 *
 */

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
