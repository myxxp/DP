package com.crabdp.mapper;

import com.crabdp.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherOrderMapper {

    void saveVoucherOrder(VoucherOrder voucherOrder);

    boolean queryVoucherOrderByUserIdAndVoucherId(Long userId, Long voucherId);
}
