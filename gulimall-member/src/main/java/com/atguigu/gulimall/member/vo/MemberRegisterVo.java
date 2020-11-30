package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.checkerframework.checker.units.qual.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class MemberRegisterVo {
    private String userName;

    private String password;

    private String phone;
}
