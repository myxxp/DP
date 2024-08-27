package com.crabdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.User;
import com.crabdp.service.UserService;
import com.crabdp.service.impl.ShopServicelmpl;
import com.crabdp.utils.RedisWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.crabdp.utils.RedisConstants.LOGIN_USER_KEY;

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
        shopServicelmpl.saveShop2Redis(1L, 10L);
    }

    //生成2000条token
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    private static final long LOGIN_USER_TTL = 30; // 过期时间，单位：分钟

    @Test
    public void testGetAll() {
        // 获取所有用户
        List<User> users = userService.list();

        // 定义文件路径
        String filePath = "/Users/crab/Desktop/tokens.txt";
        File file = new File(filePath);

        users.forEach(user -> {
            // 7.1 随机生成 token 作为登录令牌
            String token = UUID.randomUUID().toString().replace("-", "");

            // 7.2 将 User 对象转化为 UserDTO 并转为 HashMap 存储
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? "" : fieldValue.toString()));

            // 7.3 存储到 Redis
            String tokenKey = LOGIN_USER_KEY + token;
            stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

            // 7.4 设置 token 有效期
            stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

            // 将 token 写入文件
            try (FileOutputStream output = new FileOutputStream(file, true)) {
                output.write((token + System.lineSeparator()).getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error writing token to file", e);
            }
        });
    }
}
