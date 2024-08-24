package com.crabdp.service;

import com.crabdp.entity.Voucher;

import java.util.List;

public interface VoucherService {
    void addSeckillVoucher(Voucher voucher);

    List<Voucher>  queryVoucherOfShop(Long shopId);
}
