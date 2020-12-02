package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }



    @GetMapping("/boss")
    public String employees(Model model, HttpSession httpSession, @RequestParam(value = "token", required = false) String token) {
        if (!StringUtils.isEmpty(token)) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();

            httpSession.setAttribute("loginUser", body);
        }

        Object loginUser = httpSession.getAttribute("loginUser");
        if (loginUser==null) {

            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("John");
            emps.add("Hug");

            model.addAttribute("emps",emps);

            return "list";
        }

    }
}
