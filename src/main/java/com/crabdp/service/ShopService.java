package com.crabdp.service;

import com.crabdp.common.PageResult;
import com.crabdp.entity.Shop;

import java.util.List;

public interface ShopService {


    Shop queryById(Long id);

    void saveShop(Shop shop);

    void update(Shop shop);

    List<Shop> queryByTypeNoLocation(Long typeId, Integer current);

    List<Shop> queryByTypeAndLocation(Long typeId, Integer current, Double x, Double y);


    List<Shop> queryByName(String name, Integer current);
}
