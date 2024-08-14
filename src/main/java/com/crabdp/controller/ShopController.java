package com.crabdp.controller;

import com.crabdp.common.Result;
import com.crabdp.entity.Shop;
import com.crabdp.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;



@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopController {

    @Autowired
    public ShopService shopService;

    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @GetMapping("/{id}")
    public Result<Shop> queryShopById(@PathVariable("id") Long id) {
        Shop shop = shopService.queryById(id);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    /**
     * 新增商铺信息
     * @param shop 商铺数据
     * @return 商铺id
     */
    @PostMapping
    public Result<Long> saveShop(@RequestBody Shop shop) {
        shopService.saveShop(shop);
        return Result.ok(shop.getId());
    }

    /**
     * 更新商铺信息
     * @param shop 商铺数据
     * @return 无
     */
    @PutMapping
    public Result<Void> updateShop(@RequestBody Shop shop) {
        shopService.update(shop);
        return Result.ok();
    }

    @GetMapping("/of/type")
    public Result<List<Shop>> queryShopByType(
            @RequestParam("typeId") Long typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y){
        if ( x == null || y == null){
            List<Shop> shops =  shopService.queryByTypeNoLocation(typeId, current);
            return Result.ok(shops);
        }else{
            List<Shop> shops = shopService.queryByTypeAndLocation(typeId, current, x, y);
            log.info("shops:{}", shops);
            return Result.ok(shops);
        }
    }

    /**
     * 根据商铺名称关键字分页查询商铺信息
     * @param name 商铺名称
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/name")
    public Result<List<Shop>> queryShopByName(
            @RequestParam("name") String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current){
        List<Shop> shops = shopService.queryByName(name, current);
        log.info("shops:{}", shops);
        return Result.ok(shops);
    }




}
