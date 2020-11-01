package com.atguigu.gulimall.warehouse.dao;

import com.atguigu.gulimall.warehouse.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author ChaoliTan
 * @email ctan7749@gmail.com
 * @date 2020-10-15 16:18:50
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
