package com.atguigu.gulimall.order.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.FareVo;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-warehouse")
public interface WareFeignService {

    @RequestMapping("warehouse/waresku/getSkuHasStocks")
    List<SkuHasStockVo> getSkuHasStocks(@RequestBody List<Long> ids);

    @RequestMapping("warehouse/wareinfo/fare/{addrId}")
    FareVo getFare(@PathVariable("addrId") Long addrId);

    @RequestMapping("warehouse/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo itemVos);
}
