# Gulimall Online Shopping Mall (Distributed System)



## Introduction



A practice project to build a online shopping mall. It has the following interface

![home](/Eno/gulimall/docs/img/home.png)



The architecture of this project is shown below.



![谷粒商城-微服务架构图](/Eno/gulimall/docs/img/谷粒商城-微服务架构图.jpg)



## Steps to start project

```shell
1. Start Nacos and Sentinel
2. Start docker (Already set up auto start for: MySQL, ElasticSearch, Kibana, Nginx, Redis, RabbitMQ, Zipkin)
3. Start idea
4. Run corresponding service.
```





## Summary for the fundamental part

### 1.Concepts of Distributed System

　　　　Microservice, Registry Center (Nacos), Configuration Center (Nacos), Remote Call, Feign, Gateway
      Note:
        Nacos: Dynamic Naming and Configuration Service, similar to Consul/Eureka/Zookeeper.
### 2.Development Tools

　　　　SpringBoot2.0, SpringCloud, Mybatis-Plus, Vue, Element-UI, AliCloud OSS

### 3.Development Environment

　　　　Vagrant、Linux、Docker、Mysql、Redis、Reverse Engineer & Renren Open Source

### 4.Development Specification

　　　　JSR303 Bean Validation, Global Exception Handler, Global Unified Return, Global Cross-Origin Resource Sharing (CORS)

　　　  Status Enumeration, Service Status Code, Value Object, Transfer Object, and Plain Object, Logic Delete

　　　  Lombok：@Data、@Slf4j



## Summary for Advanced Part

List some framework, middleware, tools using in this part.

1. Elasticsearch
2. Render Engine
   1. Thymeleaf
3. Stress Test
   1. JMeter
4. Performance Listener
   1. jconsole vs jvisualvm
5. Dynamic and static separation
   1. Nginx
6. Cache Middleware
   1. Redis
   2. Redisson Lock
   3. consistency between cache and database
      a. dual write (Write Back)
      b. Cache Aside
      c. Write Through
   4. Spring Cache
7. Asynchronous & ThreadPool
8. Socail Login & SSO
   1. OAuth2.0
9. Spring Session
10. RabbitMQ
11. Cron Task
12. Alicloud Sentinel
13. Spring Cloud Sleuth & Zipkin
14. Interface idempotence
15. Third Party function
    1. Alipay payment
    2. Alicloud Send Sms (Short Message Service) API
    3. Weibo Sign In




## Summary for the Cluster part