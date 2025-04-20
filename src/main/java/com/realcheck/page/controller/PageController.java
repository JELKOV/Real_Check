package com.realcheck.page.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping
    public String mainPage() {
        return "index"; // 메인페이지 렌더링
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // 로그인 페이지 렌더링
    }

    // ...추가적으로 mypage, register 등도 여기에 구성
}
