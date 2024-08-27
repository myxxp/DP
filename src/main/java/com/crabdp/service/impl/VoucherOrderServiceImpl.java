package com.crabdp.service.impl;

import com.crabdp.entity.VoucherOrder;
import com.crabdp.mapper.VoucherMapper;
import com.crabdp.mapper.VoucherOrderMapper;
import com.crabdp.service.VoucherOrderService;
import com.crabdp.utils.RedisWorker;
import com.crabdp.utils.SeckillErrorCode;
import com.crabdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderServiceImpl implements VoucherOrderService{

    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private VoucherOrderMapper voucherOrderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisWorker redisWorker;
    @Resource
    private RedissonClient redissonClient;

    private VoucherOrderService proxy;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);

    }

    //create orderTasks blockqueue
    private final BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    public long seckillVoucher(Long voucherId) {
        // get user
        Long userId = UserHolder.getUser().getId();
        //1. rush lua script
        Long result =  stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );
        //2. if(result == 0)
        int r = 0;
        if (result != null) {
            r = result.intValue();
        }
        if ( r != 0) {
            return ( r == 1 ? SeckillErrorCode.OUT_OF_STOCK.getCode() : SeckillErrorCode.DUPLICATE_ORDER.getCode());
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisWorker.nextID("order");
        voucherOrder.setId(orderId);

        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        //add bolockqueue
        orderTasks.add(voucherOrder);
        proxy = (VoucherOrderService) AopContext.currentProxy();
        // TODO save
        return orderId;
    }


    //创建线程池
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderTask());
    }

    private class VoucherOrderTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    VoucherOrder voucherOrder = orderTasks.take();
                    handleVoucherOrder(voucherOrder);
                } catch (InterruptedException e) {
                    log.error("秒杀订单任务异常", e);
                }
            }
        }
    }


    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.error("获取锁失败");
            return;
        }
        try {
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            redisLock.unlock();
        }
    }


//    public long seckillVoucher(Long voucherId) {
//        log.info("秒杀券：{}", voucherId);
//        //1.查询秒杀券
//        SeckillVoucher seckillVoucher = voucherMapper.querySeckillVoucher(voucherId);
//        //2.判断秒杀是否开始
//        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            throw new RuntimeException("秒杀未开始");
//        }
//        //3.判断秒杀是否结束
//        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("秒杀已结束");
//        }
//        //4.判断库存是否足够
//        if (seckillVoucher.getStock() <= 0) {
//           return SeckillErrorCode.OUT_OF_STOCK.getCode();
//        }
//
////        //5.减库存
////        seckillVoucher.setStock(seckillVoucher.getStock() - 1);
////
////        //6.更新库存
////        voucherMapper.updateSeckillVoucher(seckillVoucher);
//        //一人一单
//        Long userId = UserHolder.getUser().getId();
//
//        //*************************分布式锁*************************
////            synchronized (userId.toString().intern()) {
////                //获取事务代理对象
////                VoucherOrderService  proxy = (VoucherOrderService) AopContext.currentProxy();
////                return proxy.createVoucherOrder(voucherId);
//
//        //*************************分布式锁*************************
//        DistributedLock distributedLock = new DistributedLockImpl("order:" + userId, stringRedisTemplate);
//        boolean isLock = distributedLock.tryLock(1200);
//        //判断获取锁是否成功
//        if (!isLock) {
//            //获取锁失败,返回重复下单错误
//            return SeckillErrorCode.DUPLICATE_ORDER.getCode();
//            }
//        try {
//            VoucherOrderService  proxy = (VoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
//            distributedLock.unlock();
//        }
//    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {

       Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        if (voucherOrderMapper.queryVoucherOrderByUserIdAndVoucherId(userId, voucherId) ) {
            //重复引用
            throw new RuntimeException("重复订单");
        }
        boolean success = voucherMapper.decreaseStock(voucherId);
        if (!success) {
            //库存不足
            throw new RuntimeException("库存不足");
        }
        voucherOrderMapper.saveVoucherOrder(voucherOrder);

    }

}
