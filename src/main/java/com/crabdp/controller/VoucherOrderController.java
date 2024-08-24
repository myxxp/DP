package com.crabdp.controller;

import com.crabdp.common.Result;
import com.crabdp.service.VoucherOrderService;
import com.crabdp.utils.SeckillErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
@Slf4j

public class VoucherOrderController {
    @Autowired
    private VoucherOrderService voucherOrderService;

    @PostMapping("seckill/{id}")
    public Result<Long> seckillVoucher(@PathVariable("id") Long voucherId) {

        long result = voucherOrderService.seckillVoucher(voucherId);
        if (result  > 0) {
            return Result.ok(result);
        }

        SeckillErrorCode errorCode = SeckillErrorCode.fromCode(result);
        return Result.fail(errorCode.getMessage());

    }

}
