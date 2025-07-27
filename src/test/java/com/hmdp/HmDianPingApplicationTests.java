package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {
    @Resource
    private ShopServiceImpl shopServiceImpl;

    @Resource
    private RedisIdWorker redisIdWorker;

    private final ExecutorService executors = Executors.newFixedThreadPool(500);

    @Test
    void testRedisIdWorker() throws InterruptedException {
        // 修改CountDownLatch初始值为100，与任务数量保持一致
        CountDownLatch latch = new CountDownLatch(3000);
        Runnable task = () -> {
            for (int i = 0; i < 300; i++) {
                long id = redisIdWorker.nextId("test");
                //System.out.println("id = " + id);
            }
            // 添加countDown调用，确保每个任务完成后减少计数器
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 3000; i++) {
            executors.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin)+"ms");
    }
}