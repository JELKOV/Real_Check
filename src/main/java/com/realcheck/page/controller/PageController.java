package com.realcheck.page.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping
    public String mainPage() {
        return "index"; // 메인페이지 렌더링
    }

    @GetMapping("/register")
    public String registerPage() {
        return "user/register"; // 회원가입 페이지 렌더링
    }

    @GetMapping("/my-logs")
    public String myPage() {
        return "status/my-logs"; // 마이페이지 (내 상태 로그 보기)
    }

    @GetMapping("/nearby")
    public String nearbyPage() {
        return "map/nearby"; // 지도 기반 상태 보기 페이지
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin/admin"; // 관리자 대시보드
    }
}
