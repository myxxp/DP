package com.crabdp.config;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: crabdp
 * @description: redisson配置类
 * @author: snow
 * @create: 2024-08-26 11:09
 **/
@Configuration
public class RedisonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        return org.redisson.Redisson.create(config);

    }
}
