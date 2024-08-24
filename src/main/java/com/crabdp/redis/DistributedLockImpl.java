package com.crabdp.redis;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @program: crabdp
 * @description: 实现分布式锁
 * @author: snow
 * @create: 2024-08-24 09:16
 **/
public class DistributedLockImpl implements DistributedLock{

    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public DistributedLockImpl(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    // 锁前缀
    private static final String LOCK_PREFIX = "lock:";
    // 线程唯一标识UUID
    private static  final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    private static  final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setResultType(Long.class);
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
    }



    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean result = stringRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX + name, threadId +"", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    //使用lua脚本解锁

    public void unlock() {
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(LOCK_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());

    }
    //    public void unlock() {
//        // 获取线程标识
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//        // 获取锁的值
//        String value = stringRedisTemplate.opsForValue().get(LOCK_PREFIX + name);
//        // 判断是否是当前线程持有的锁
//        if (threadId.equals(value)) {
//            // 释放锁
//            stringRedisTemplate.delete(LOCK_PREFIX + name);
//        }


    }

