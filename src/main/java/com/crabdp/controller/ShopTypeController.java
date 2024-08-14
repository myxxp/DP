package com.crabdp.controller;

import com.crabdp.common.Result;
import com.crabdp.entity.ShopType;
import com.crabdp.service.ShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Autowired
    private ShopTypeService shopTypeService;


    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> typeList = shopTypeService.queryTypeList();
        return Result.ok(typeList);
    }

}
