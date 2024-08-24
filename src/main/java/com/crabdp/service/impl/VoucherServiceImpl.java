package com.crabdp.service.impl;

import com.crabdp.entity.SeckillVoucher;
import com.crabdp.entity.Voucher;
import com.crabdp.mapper.VoucherMapper;
import com.crabdp.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.crabdp.utils.RedisConstants.SECKILL_STOCK_KEY;

@Service
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherMapper voucherMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addSeckillVoucher(Voucher voucher) {
        log.info("添加秒杀券：{}", voucher);
        voucherMapper.saveVoucher(voucher);
        //保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        voucherMapper.saveSeckillVoucher(seckillVoucher);
        //保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());




    }

    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        return voucherMapper.queryVoucherOfShop(shopId);
    }


}
