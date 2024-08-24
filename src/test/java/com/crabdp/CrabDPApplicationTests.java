package com.crabdp;

import com.crabdp.service.impl.ShopServicelmpl;
import com.crabdp.utils.RedisWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CrabDPApplicationTests {

    @Autowired
    private ShopServicelmpl shopServicelmpl;
    @Resource
    private RedisWorker redisWorker;

    private ExecutorService executorService = Executors.newFixedThreadPool(500);


    @Test
    public void testIdWorker() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisWorker.nextID("order");
                //id 转为2进制
               String idStr = Long.toBinaryString(id);
                System.out.println(idStr);
            }
            countDownLatch.countDown();

        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - begin));
    }



    @Test
    public void testSaveShop() throws InterruptedException {
       shopServicelmpl.saveShop2Redis(1L,10L);
    }
}
