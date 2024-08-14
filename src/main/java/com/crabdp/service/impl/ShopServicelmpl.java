package com.crabdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crabdp.common.PageResult;
import com.crabdp.entity.Shop;
import com.crabdp.mapper.ShopMapper;
import com.crabdp.service.ShopService;
import com.crabdp.utils.CacheClient;
import com.crabdp.utils.RedisData;
import com.crabdp.utils.SystemConstants;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cn.hutool.db.sql.SqlExecutor.query;
import static com.crabdp.utils.RedisConstants.*;

@Service
public class ShopServicelmpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;
    @Override
    public Shop queryById(Long id) {


        //使用Redis封装类-缓存设为空值--解决缓存穿透 传入key前缀，id，返回类型，数据库查询方法，过期时间，时间单位
//        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, shopMapper::queryById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        //使用Redis封装类-逻辑过期解决缓存击穿 传入key前缀，id，返回类型，数据库查询方法，过期时间，时间单位
        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, shopMapper::queryById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //使用Redis封装类-互斥锁解决缓存击穿 传入key前缀，id，返回类型，数据库查询方法，过期时间，时间单位
        //Shop shop = cacheClient.queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, shopMapper::queryById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        Shop shop = shopMapper.queryById(id);
        // 解决缓存穿透
     //   Shop shop = queryWithPassThrough(id);

         //互斥锁解决缓存击穿
//        Shop shop = queryWithMutex(id);



        // 逻辑过期解决缓存击穿
//        Shop shop = queryWithLogicalExpire(id);

        return shop;
    }

    public Shop queryWithPassThrough(Long id) {
        String key = "CACHE_SHOP_KEY" + id;
        //1.redis中查询商铺信息缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.如果缓存中有数据，直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        if (shopJson != null) {
            throw new IllegalArgumentException("店铺不存在");
        }
        //3.如果缓存中没有数据，查询数据库
        Shop shop = shopMapper.queryById(id);
        // 判断是否存在
        if (shop == null) {
            // 防止缓存穿透 空值写入redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            throw new IllegalArgumentException("店铺不存在");
        }

        //4.将查询到的数据存入缓存,设置过期时间30min
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);



        return shop;
    }


    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        //1.redis中查询商铺信息缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.如果缓存中有数据，直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        if (shopJson != null) {
            throw new IllegalArgumentException("店铺不存在");
        }

        //4.实现缓存重建
        //4.1.获取互斥锁
        String lockKey = "LOCK_SHOP_KEY" + id;


        Shop shop;
        try {
            boolean isLock = tryLock(lockKey);
            //4.2.判断是否获取成功
            if (!isLock) {
                //4.3.获取锁失败，休眠并重试

                Thread.sleep(50);
                return queryWithMutex(id);
            }
                //4.4.获取锁成功，根据id查询数据库
                shop = shopMapper.queryById(id);
            Thread.sleep(200);
            // 5.判断是否存在
            if (shop == null) {
                // 防止缓存穿透 空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                throw new IllegalArgumentException("店铺不存在");
            }

            //6.存在，写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //7.释放锁
            unlock(lockKey);
        }
        return shop;
    }
    //建立缓存重建线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public Shop queryWithLogicalExpire(Long id){
        String key = CACHE_SHOP_KEY + id;
        //1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StrUtil.isBlank(shopJson)) {
            //3.存在，直接返回
            throw new IllegalArgumentException("店铺不存在");
        }
        //4.命中，json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //5.1.未过期，直接返回店铺信息
            return shop;
        }
        //5.2.已过期，需要缓存重建
        //6.缓存重建
        //6.1.获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        //6.2.判断是否获取锁成功
        if (isLock){
            //6.3.成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {

                    // 重建缓存
                    this.saveShop2Redis(id, CACHE_SHOP_TTL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        //6.4.返回过期的商铺信息
        return shop;
    }

    @Override
    public void saveShop(Shop shop) {
        shopMapper.saveShop(shop);
    }

    public void update(Shop shop) {
        if (shop.getId() == null) {
            throw new IllegalArgumentException("店铺id不能为空");
        }
        //1.更新数据库
        shopMapper.update(shop);
        //2.删除缓存
        String key = "CACHE_SHOP_KEY" + shop.getId();
        stringRedisTemplate.delete(key);

    }

    public List<Shop> queryByTypeNoLocation(Long typeId, Integer current) {

        PageHelper.startPage(current, SystemConstants.DEFAULT_PAGE_SIZE);

        Page<Shop> page = shopMapper.pageQueryByType(typeId);

        long total = page.getTotal();
        List<Shop> records = page.getResult();

        return records;




    }

    @Override
    public List<Shop> queryByTypeAndLocation(Long typeId, Integer current, Double x, Double y) {
//        // Redis key for the geo data
//        String key = "SHOP_GEO_KEY_" + typeId;
//
//        // Page parameters
//        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
//        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
//
//        // Execute geo search
//        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
//                .search(
//                        key,
//                        GeoReference.fromCoordinate(x, y),
//                        new Distance(5000),
//                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
//                );
//
//        if (results == null || results.getContent().isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
//        if (list.size() <= from) {
//            return Collections.emptyList();
//        }
//
//        List<Long> ids = new ArrayList<>(list.size());
//        Map<String, Distance> distanceMap = new HashMap<>(list.size());
//        list.stream().skip(from).forEach(result -> {
//            String shopIdStr = result.getContent().getName();
//            ids.add(Long.valueOf(shopIdStr));
//            Distance distance = result.getDistance();
//            distanceMap.put(shopIdStr, distance);
//        });
//
//        // Fetch shop details from the database
//
//        List<Shop> shops = shopMapper.findByIds(ids);
//        for (Shop shop : shops) {
//            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
//        }
//
//        return shops;
        PageHelper.startPage(current, SystemConstants.DEFAULT_PAGE_SIZE);
        Page<Shop> page = shopMapper.queryByTypeAndLocation(typeId, x, y);
        return page.getResult();
    }

    public List<Shop> queryByName(String name, Integer current) {
        PageHelper.startPage(current, SystemConstants.DEFAULT_PAGE_SIZE);
        Page<Shop> page = shopMapper.queryByName(name);
        return page.getResult();
    }


    /**
     * 尝试获取锁
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     * @param key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        //1.查询数据库
        Shop shop = shopMapper.queryById(id);
        Thread.sleep(200);
        //2. 封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.存入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY  + id, JSONUtil.toJsonStr(redisData));

    }
}
