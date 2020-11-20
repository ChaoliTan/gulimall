package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-warehouse")
public interface WareFeignService {

    /**
     * 1. R 设计的时候可以加上范型
     * 2. 直接返回我们想要的结果
     * 2. 自己封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/warehouse/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
