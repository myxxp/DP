package com.crabdp.entity;

import lombok.Data;

import java.util.List;

/**
 * @program: crabdp
 * @description: 有序列表滚动分页查询分数结果类
 * @author: snow
 * @create: 2024-08-28 19:56
 **/
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
