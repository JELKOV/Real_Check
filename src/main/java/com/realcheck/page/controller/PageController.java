package com.realcheck.page.controller;

import com.realcheck.user.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PageController
 * - 단순 JSP 페이지 이동을 담당하는 컨트롤러
 */
@Controller
public class PageController {

    // ─────────────────────────────────────────────
    // [1] 사용자 일반 페이지
    // ─────────────────────────────────────────────

    @GetMapping
    public String mainPage() {
        return "index";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "user/mypage";
    }

    @GetMapping("/my-logs")
    public String myLogsPage() {
        return "status/my-logs";
    }

    @GetMapping("/edit-profile")
    public String editProfilePage() {
        return "user/edit-profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "user/change-password";
    }

    // ─────────────────────────────────────────────
    // [2] 요청 관련 페이지
    // ─────────────────────────────────────────────

    //요청 등록 페이지 (지도에서 질문 등록)
    @GetMapping("/request/register")
    public String requestRegisterPage() {
        return "request/register";
    }

    //요청 목록 페이지 (답변 가능한 요청 리스트)
    @GetMapping("/request/list")
    public String requestListPage() {
        return "request/list";
    }

    //요청 상세 페이지
    @GetMapping("/request/{id}")
    public String requestDetailPage() {
        return "request/detail";
    }

    // ─────────────────────────────────────────────
    // [3] 지도 관련 페이지
    // ─────────────────────────────────────────────

    @GetMapping("/nearby")
    public String nearbyPage() {
        return "map/nearby";
    }

    /** 주변 요청 확인 (답변할 요청) */
    @GetMapping("/nearby/request-list")
    public String nearbyRequestListPage() {
        return "map/request-list";
    }

    // ─────────────────────────────────────────────
    // [4] 관리자 페이지
    // ─────────────────────────────────────────────

    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        return isAdmin(session) ? "admin/admin" : "redirect:/login?error=unauthorized";
    }

    @GetMapping("/admin/stats")
    public String adminStatsPage(HttpSession session) {
        return isAdmin(session) ? "admin/stats" : "redirect:/login?error=unauthorized";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage(HttpSession session) {
        return isAdmin(session) ? "admin/users" : "redirect:/login?error=unauthorized";
    }

    @GetMapping("/admin/reports")
    public String adminReportsPage(HttpSession session) {
        return isAdmin(session) ? "admin/reports" : "redirect:/login?error=unauthorized";
    }

    // ─────────────────────────────────────────────
    // [5] 내부 메서드
    // ─────────────────────────────────────────────

    private boolean isAdmin(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        return loginUser != null && "ADMIN".equals(loginUser.getRole());
    }
}
