package com.realcheck.page.controller;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * PageController
 * - 단순 JSP 페이지 이동을 담당하는 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;

    @Value("${naver.map.client.id}")
    private String naverMapClientId;

    // ─────────────────────────────────────────────
    // [1] 사용자 일반 페이지
    // ─────────────────────────────────────────────

    // 메인페이지 (진입페이지)
    @GetMapping
    public String mainPage() {
        return "index";
    }

    // 마이페이지 - page: common/header.jsp
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 최근 활동 불러오기
        List<Map<String, Object>> recentActivities = userService.getRecentActivities(loginUser.getId());

        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("loginUser", loginUser);

        return "user/mypage";
    }

    // 내 응답 관련 페이지 - page: common/header.jsp
    @GetMapping("/my-logs")
    public String myLogsPage() {
        return "status/my-logs";
    }

    // 마이페이지 -> 수정 - page: user/mypage.jsp
    @GetMapping("/edit-profile")
    public String editProfilePage() {
        return "user/edit-profile";
    }

    // 마이페이지 -> 비밀번호 변경 - page: user/mypage.jsp
    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "user/change-password";
    }

    // ─────────────────────────────────────────────
    // [2] 요청 관련 페이지
    // ─────────────────────────────────────────────

    // 요청 등록 페이지 (지도에서 질문 등록) - page : index.jsp
    @GetMapping("/request/register")
    public String requestRegisterPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);
        return "request/register";
    }

    // 요청 목록 페이지 (답변 가능한 요청 리스트) - page : index.jsp
    @GetMapping("/request/list")
    public String requestListPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "request/list";
    }

    // 요청 상세 페이지
    // page:
    // (1) request/list.jsp
    // (2) map/request-list.jsp
    // (3) request/my-requests.jsp
    @GetMapping("/request/{id}")
    public String requestDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("requestId", id);
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "request/detail";
    }

    // 내 요청 관련 페이지 - page: header.jsp
    @GetMapping("/my-requests")
    public String myRequestsPage() {
        return "request/my-requests";
    }

    // ─────────────────────────────────────────────
    // [3] 지도 관련 페이지
    // ─────────────────────────────────────────────

    // 주변 졍보 확인 (답변 달린 내용) - page: index.jsp
    @GetMapping("/nearby")
    public String nearbyPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "map/nearby";
    }

    // 주변 요청 확인 (답변할 요청) - page: index.jsp
    @GetMapping("/nearby/request-list")
    public String nearbyRequestListPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "map/request-list";
    }

    // ─────────────────────────────────────────────
    // [4] 관리자 페이지
    // ─────────────────────────────────────────────

    // 관라자페이지 이동 - page: common/header.jsp
    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        return isAdmin(session) ? "admin/admin" : "redirect:/login?error=unauthorized";
    }

    // 관리자 통계보기 페이지 - page: admin/admin.jsp
    @GetMapping("/admin/stats")
    public String adminStatsPage(HttpSession session) {
        return isAdmin(session) ? "admin/stats" : "redirect:/login?error=unauthorized";
    }

    // 관리자 사용자관리 페이지 - page: admin/admin.jsp
    @GetMapping("/admin/users")
    public String adminUsersPage(HttpSession session) {
        return isAdmin(session) ? "admin/users" : "redirect:/login?error=unauthorized";
    }

    // 관리자 신고관리 페이지 - page: admin/admin.jsp
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
