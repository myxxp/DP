package com.crabdp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Voucher {



        private static final long serialVersionUID = 1L;

        /**
         * 主键
         */
        private Long id;

        /**
         * 商铺id
         */
        private Long shopId;

        /**
         * 代金券标题
         */
        private String title;

        /**
         * 副标题
         */
        private String subTitle;

        /**
         * 使用规则
         */
        private String rules;

        /**
         * 支付金额
         */
        private Long payValue;

        /**
         * 抵扣金额
         */
        private Long actualValue;

        /**
         * 优惠券类型
         */
        private Integer type;

        /**
         * 优惠券类型
         */
        private Integer status;
        /**
         * 库存
         * 数据库中没有这个字段
         */
        private Integer stock;

        /**
         * 生效时间
         * 数据库中没有这个字段
         */
        private LocalDateTime beginTime;
        /**
         * 失效时间
         * 数据库中没有这个字段
         */
        private LocalDateTime endTime;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;


        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

}
