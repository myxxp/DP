package com.crabdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        long orderId = redisWorker.nextID("order");
        //1. rush lua script
        Long result =  stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        //2. if(result == 0)
        int r = 0;
        if (result != null) {
            r = result.intValue();
        }
        if ( r != 0) {
            return ( r == 1 ? SeckillErrorCode.OUT_OF_STOCK.getCode() : SeckillErrorCode.DUPLICATE_ORDER.getCode());
        }

        proxy = (VoucherOrderService) AopContext.currentProxy();

        return orderId;
    }


    //创建线程池
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderTask());
    }

    //定义队列名称
    private final String queueName = "stream.orders";
    private class VoucherOrderTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    //1.获取消息队列中的订单信息, XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS streams.order >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    //2.判断消息获取是否成功
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    //3.解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);//获取第一条消息
                    Map<Object, Object> values = record.getValue();//获取消息中的数据
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    //4.处理订单
                    handleVoucherOrder(voucherOrder);
                    //5.ACK确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("秒杀订单任务异常", e);
                    //避免频繁抛出异常,休眠一下
                    handlePendingList();
                }
            }
        }
    }

    /**
     * 处理积压的订单
     * 1.获取消息队列中的订单信息, XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS streams.order
     *
     */

    private void handlePendingList() {
        while(true) {
            try {
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queueName, ReadOffset.from("0"))
                );
                //2.判断消息获取是否成功
                if (list == null || list.isEmpty()) {
                    break;
                }
                //3.解析消息中的订单信息
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> values = record.getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                //4.如果获取成功,可以下单
                handleVoucherOrder(voucherOrder);
                //5.ACK确认
                stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
            } catch (Exception e) {
                log.error("秒杀订单任务异常", e);
                //避免频繁抛出异常,休眠一下
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
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




    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {

        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
       //尝试获取锁
        boolean isLock = redisLock.tryLock();

        if (!isLock) {
            //获取锁失败
            log.error("获取锁失败");
            return;
        }

        try {
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
        } finally {
            //释放锁
            redisLock.unlock();

        }

    }

}
