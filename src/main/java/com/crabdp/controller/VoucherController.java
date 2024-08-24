package com.crabdp.controller;

import com.crabdp.common.Result;
import com.crabdp.entity.Voucher;
import com.crabdp.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/voucher")
public class VoucherController {
    @Resource
    private VoucherService voucherService;

    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        List<Voucher> vouchers = voucherService.queryVoucherOfShop(shopId);
        return Result.ok(vouchers);
    }

}
