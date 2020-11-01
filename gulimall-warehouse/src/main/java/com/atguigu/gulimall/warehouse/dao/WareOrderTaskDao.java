package com.atguigu.gulimall.warehouse.dao;

import com.atguigu.gulimall.warehouse.entity.WareOrderTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存工作单
 * 
 * @author ChaoliTan
 * @email ctan7749@gmail.com
 * @date 2020-10-15 16:18:50
 */
@Mapper
public interface WareOrderTaskDao extends BaseMapper<WareOrderTaskEntity> {
	
}
