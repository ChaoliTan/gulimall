package com.atguigu.gulimall.seckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：
 *      1. @EnableScheduling 开启定时任务
 *      2. @Scheduled
 *      3. AutoConfiguration Class: TaskSchedulingAutoConfiguration
 *      4. Attributes binding: TaskSchedulingProperties
 *
 * 异步任务：
 *      1. @EnableAsync
 *      2. @Async
 *      3. AutoConfiguration Class: TaskExecutionAutoConfiguration
 *      4. Attributes binding: TaskExecutionProperties
 */

@Slf4j
//@EnableAsync
@EnableScheduling
@Component
public class HelloSchedule {

    /**
     * 1. Spring 中cron由6位组成，不允许第7位
     * 2. 1-7分别代表：MON-SUN
     * 3. 定时任务不应该是阻塞的。默认是阻塞的
     *      1）可以让业务运行以异步的方式，自己提交到线程池
     *      2）支持定时任务线程池
     *
     *      3）让定时任务异步执行
     *          异步任务：
     *
     *      解决：使用异步任务+定时任务实现不阻塞的功能
     * @throws InterruptedException
     */
//    @Scheduled(cron = "0 0 3 * * ?")
    @Async
    @Scheduled(cron = "* * * * * 3")
    public void hello() throws InterruptedException {
        Thread.sleep(3000);
        log.info("hello...");
    }
}
