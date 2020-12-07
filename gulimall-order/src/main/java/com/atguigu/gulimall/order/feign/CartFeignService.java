package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {

    @ResponseBody
    @RequestMapping("/getCheckedItems")
    List<OrderItemVo> getCheckedItems();
}