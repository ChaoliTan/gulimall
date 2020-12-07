package com.atguigu.gulimall.warehouse.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String OrderSn;

    private List<OrderItemVo> locks;
}