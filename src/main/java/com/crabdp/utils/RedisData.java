package com.crabdp.utils;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RedisData {

    // 逻辑过期时间
    private LocalDateTime expireTime;
    private Object Data;
}
