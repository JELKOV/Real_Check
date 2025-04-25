package com.realcheck.page.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;

@Controller
public class PageController {

    @GetMapping
    public String mainPage() {
        return "index"; // 메인페이지 렌더링
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "user/mypage";
    }

    @GetMapping("/my-logs")
    public String myPage() {
        return "status/my-logs"; // 마이페이지 (내 상태 로그 보기)
    }

    @GetMapping("/nearby")
    public String nearbyPage() {
        return "map/nearby"; // 지도 기반 상태 보기 페이지
    }

    @GetMapping("/edit-profile")
    public String editProfilePage() {
        return "user/edit-profile"; // 정보 수정정
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "user/change-password"; // 비밀번호 변경
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/login?error=unauthorized";
        }
        return "admin/admin";
    }

    @GetMapping("/admin/stats")
    public String adminStatsPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/login?error=unauthorized";
        }
        return "admin/stats";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/login?error=unauthorized";
        }
        return "admin/users";
    }

    @GetMapping("/admin/reports")
    public String adminReportsPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/login?error=unauthorized";
        }
        return "admin/reports";
    }
}
