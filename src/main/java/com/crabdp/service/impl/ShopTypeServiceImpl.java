package com.crabdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.crabdp.entity.ShopType;
import com.crabdp.mapper.ShopTypeMapper;
import com.crabdp.service.ShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShopTypeServiceImpl implements ShopTypeService {

    @Autowired
    private ShopTypeMapper shopTypeMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public List<ShopType> queryTypeList() {

       String key = "shopTypeList";
        //1.从Redis缓存中查询数据
        Long size = stringRedisTemplate.opsForList().size(key);

        //2.如果缓存中有数据，直接返回
        if ( size != null && size > 0) {
            List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(key, 0, -1);
            if (shopTypeJsonList != null && shopTypeJsonList.size() > 0) {
                return shopTypeJsonList.stream()
                        .map(json -> JSONUtil.toBean(json, ShopType.class))
                        .collect(Collectors.toList());
            }
        }


        //3.如果缓存中没有数据，查询数据库
        List<ShopType> shopTypes = shopTypeMapper.queryTypeList();
        //4.将查询到的数据存入缓存
        if (shopTypes != null && !shopTypes.isEmpty()) {
            List<String> shopTypeJsonList = shopTypes.stream()
                    .map(JSONUtil::toJsonStr)
                    .collect(Collectors.toList());
            stringRedisTemplate.opsForList().leftPushAll(key, shopTypeJsonList);
            stringRedisTemplate.expire(key, 30, java.util.concurrent.TimeUnit.MINUTES);//设置过期时间

        }


        return shopTypes;

//        // 1.从Redis缓存中查询数据
//        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.如果缓存中有数据，直接返回
//        if (shopTypeJson != null) {
//            return JSONUtil.toList(shopTypeJson, ShopType.class);
//        }
//
//        // 3.如果缓存中没有数据，查询数据库
//        List<ShopType> shopTypes = shopTypeMapper.queryTypeList();
//
//        // 4.将查询到的数据存入缓存并设置过期时间
//        if (shopTypes != null && !shopTypes.isEmpty()) {
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypes), 30, TimeUnit.MINUTES);
//        }

//        return shopTypes;
    }

}
