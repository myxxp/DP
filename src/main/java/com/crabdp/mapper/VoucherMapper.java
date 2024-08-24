package com.crabdp.mapper;

import com.crabdp.entity.SeckillVoucher;
import com.crabdp.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoucherMapper {
    //保存秒杀券
    void saveVoucher(Voucher voucher);

    //保存秒杀信息
    void saveSeckillVoucher(SeckillVoucher seckillVoucher);

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);

    SeckillVoucher querySeckillVoucher(Long voucherId);

    void updateSeckillVoucher(SeckillVoucher seckillVoucher);
    //减少库存的原子操作，保证线程安全
    boolean decreaseStock(Long voucherId);
}
