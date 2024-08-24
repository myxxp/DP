package com.crabdp.service.impl;

import com.crabdp.entity.SeckillVoucher;
import com.crabdp.entity.VoucherOrder;
import com.crabdp.mapper.VoucherMapper;
import com.crabdp.mapper.VoucherOrderMapper;
import com.crabdp.redis.DistributedLock;
import com.crabdp.redis.DistributedLockImpl;
import com.crabdp.service.VoucherOrderService;
import com.crabdp.utils.RedisWorker;
import com.crabdp.utils.SeckillErrorCode;
import com.crabdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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



    public long seckillVoucher(Long voucherId) {
        log.info("秒杀券：{}", voucherId);
        //1.查询秒杀券
        SeckillVoucher seckillVoucher = voucherMapper.querySeckillVoucher(voucherId);
        //2.判断秒杀是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("秒杀未开始");
        }
        //3.判断秒杀是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("秒杀已结束");
        }
        //4.判断库存是否足够
        if (seckillVoucher.getStock() <= 0) {
           return SeckillErrorCode.OUT_OF_STOCK.getCode();
        }

//        //5.减库存
//        seckillVoucher.setStock(seckillVoucher.getStock() - 1);
//
//        //6.更新库存
//        voucherMapper.updateSeckillVoucher(seckillVoucher);
        //一人一单
        Long userId = UserHolder.getUser().getId();

        //*************************分布式锁*************************
//            synchronized (userId.toString().intern()) {
//                //获取事务代理对象
//                VoucherOrderService  proxy = (VoucherOrderService) AopContext.currentProxy();
//                return proxy.createVoucherOrder(voucherId);

        //*************************分布式锁*************************
        DistributedLock distributedLock = new DistributedLockImpl("order:" + userId, stringRedisTemplate);
        boolean isLock = distributedLock.tryLock(1200);
        //判断获取锁是否成功
        if (!isLock) {
            //获取锁失败,返回重复下单错误
            return SeckillErrorCode.DUPLICATE_ORDER.getCode();
            }
        try {
            VoucherOrderService  proxy = (VoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            distributedLock.unlock();
        }
    }

    @Transactional
    public long createVoucherOrder(Long voucherId) {
       Long userId = UserHolder.getUser().getId();
        if (voucherOrderMapper.queryVoucherOrderByUserIdAndVoucherId(userId, voucherId) ) {
            //重复引用
            return SeckillErrorCode.DUPLICATE_ORDER.getCode();
        }
        boolean success = voucherMapper.decreaseStock(voucherId);
        if (!success) {
            //库存不足
            return SeckillErrorCode.OUT_OF_STOCK.getCode();
        }


        //7.生成订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //7.1 订单号
        long orderId = redisWorker.nextID("voucher_order");
        voucherOrder.setId(orderId);
        //7.2 用户ID
        Long userID = UserHolder.getUser().getId();
        voucherOrder.setUserId(userID);
        //7.3 优惠券ID
        voucherOrder.setVoucherId(voucherId);
        voucherOrderMapper.saveVoucherOrder(voucherOrder);
        return orderId;

    }

}
