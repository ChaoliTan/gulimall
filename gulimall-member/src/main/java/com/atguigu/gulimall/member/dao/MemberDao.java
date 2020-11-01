package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author ChaoliTan
 * @email ctan7749@gmail.com
 * @date 2020-10-15 12:45:39
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
