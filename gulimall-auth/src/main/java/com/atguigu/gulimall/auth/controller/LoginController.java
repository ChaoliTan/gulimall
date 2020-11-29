package com.atguigu.gulimall.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        return "login";
    }


    @GetMapping("/register.html")
    public String registerPage(HttpSession session) {
        return "register";
    }

}
