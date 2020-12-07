package com.atguigu.gulimall.warehouse.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.warehouse.dao.WareInfoDao;
import com.atguigu.gulimall.warehouse.entity.WareInfoEntity;
import com.atguigu.gulimall.warehouse.feign.MemberFeignService;
import com.atguigu.gulimall.warehouse.service.WareInfoService;
import com.atguigu.gulimall.warehouse.vo.FareVo;
import com.atguigu.gulimall.warehouse.vo.MemberAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                                      .like("name",key)
                                      .or().like("address",key)
                                      .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.info(addrId);
        if (info.getCode() == 0) {
            MemberAddressVo address = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
            });
            fareVo.setAddress(address);
            String phone = address.getPhone();
            //取电话号的最后两位作为邮费
            String fare = phone.substring(phone.length() - 2, phone.length());
            fareVo.setFare(new BigDecimal(fare));
        }
        return fareVo;
    }

}