package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
        // 抗修改性：彩虹表
        String s = DigestUtils.md5Hex("123");
        System.out.println(s);

        // 盐值加密
        String s1 = Md5Crypt.md5Crypt("123".getBytes(),"$1$qqqqqqqq");
        System.out.println(s1);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123");
        System.out.println(encode);

        boolean matches = passwordEncoder.matches("123", "$2a$10$AFN.Z4L4pEXQCMPgTId5/ehxrUhyAgFXj4u1107iEFPogF.5L81Mm");

        System.out.println(matches);

    }

}
