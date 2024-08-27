package com.crabdp.service;

import com.crabdp.entity.VoucherOrder;

public interface VoucherOrderService {
    long seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder voucherId);
}
