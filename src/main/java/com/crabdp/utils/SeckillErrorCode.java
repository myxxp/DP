package com.crabdp.utils;

/**
 * @program: crabdp
 * @description: 秒杀券错误代码类
 * @author: snow
 * @create 2024-08-21 14:32
 **/
public enum SeckillErrorCode {
    OUT_OF_STOCK(-1, "库存不足"),
    DUPLICATE_ORDER(-2, "重复订单"),
    SYSTEM_ERROR(-99, "系统错误");

    private final long code;
    private final String message;

    SeckillErrorCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static SeckillErrorCode fromCode(long code) {
        for (SeckillErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR; // 如果没有匹配到，返回系统错误
    }
}
