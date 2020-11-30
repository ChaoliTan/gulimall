package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimall-member")  //,fallback = MemberFallbackService.class)
public interface MemberFeignService {

    @RequestMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo registerVo);

//    @RequestMapping("member/member/login")
//    R login(@RequestBody UserLoginVo loginVo);
//
//    @RequestMapping("member/member/oauth2/login")
//    R login(@RequestBody SocialUser socialUser);
}