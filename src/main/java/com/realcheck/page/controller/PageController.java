package com.realcheck.page.controller;

import com.realcheck.common.dto.PageResult;
import com.realcheck.place.dto.PlaceDetailsDto;
import com.realcheck.place.service.PlaceService;
import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.service.RequestService;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PageController
 * - 단순 JSP 페이지 이동을 담당하는 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;
    private final PlaceService placeService;
    private final StatusLogService statusLogService;
    private final RequestService requestService;

    @Value("${naver.map.client.id}")
    private String naverMapClientId;

    // ─────────────────────────────────────────────
    // [1] 사용자 일반 페이지
    // ─────────────────────────────────────────────

    /**
     * [1-1] 메인페이지 (진입페이지)
     */
    @GetMapping
    public String mainPage() {
        return "index";
    }

    /**
     * [1-2] 마이페이지
     * page: common/header.jsp
     */
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

    /**
     * [1-3] 탈퇴 예약 상태 페이지 이동
     * page user/login.jsp
     * - LoginController 또는 AccountRestrictionController에서
     */
    @GetMapping("/account-restricted")
    public String accountRestrictedPage(HttpSession session, Model model) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 탈퇴 예정 시간 전달
        model.addAttribute("deletionScheduledAt",
                loginUser.getDeletionScheduledAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
        return "user/account-restricted";
    }

    /**
     * [1-4] 내 응답 관련 페이지
     * page: common/header.jsp
     */
    @GetMapping("/my-logs")
    public String myLogsPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        return "status/my-logs";
    }

    /**
     * [1-5] 내 정보 수정 페이지
     * page: user/mypage.jsp
     */
    @GetMapping("/edit-profile")
    public String editProfilePage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        return "user/edit-profile";
    }

    /**
     * [1-6] 내 비밀번호 변경 페이지
     * page: user/mypage.jsp
     */
    @GetMapping("/change-password")
    public String changePasswordPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        return "user/change-password";
    }

    // ─────────────────────────────────────────────
    // [2] 요청 /응답 관련 페이지
    // ─────────────────────────────────────────────

    /**
     * [2-1] 요청 등록 페이지 (지도에서 질문 등록)
     * page : index.jsp
     */
    @GetMapping("/request/register")
    public String requestRegisterPage(HttpSession session, Model model) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("naverMapClientId", naverMapClientId);
        return "request/register";
    }

    /**
     * [2-2] 요청 목록 페이지 (답변 가능한 요청 리스트)
     * page : index.jsp
     */
    @GetMapping("/request/list")
    public String requestListPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "request/list";
    }

    /**
     * [2-3] 요청 상세 페이지
     * page: request/list.jsp
     * page: map/request-list.jsp
     * page: request/my-requests.jsp
     */
    @GetMapping("/request/{id}")
    public String requestDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("requestId", id);
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "request/detail";
    }

    /**
     * [2-4] 내 요청 관련 페이지
     * page: header.jsp
     */
    @GetMapping("/my-requests")
    public String myRequestsPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        return "request/my-requests";
    }

    /**
     * [2-5] 공지 등록, 페이지로 이동 (해당 장소의 owner만 접근 가능)
     * page: place/community.jsp
     */
    @GetMapping("/status/register")
    public String showRegisterNoticePage(@RequestParam("placeId") Long placeId,
            HttpSession session,
            Model model) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        PlaceDetailsDto place = placeService.getPlaceDetails(placeId);

        // 소유자 확인
        if (!loginUser.getId().equals(place.getOwnerId())) {
            throw new IllegalStateException("공지 등록 권한이 없습니다.");
        }

        model.addAttribute("place", place);
        return "place/register";
    }

    /**
     * [2-6] 공지 수정 페이지로 이동 (해당 장소의 owner만 접근 가능)
     * page: place/community.jsp
     */
    @GetMapping("/status/edit")
    public String showEditForm(@RequestParam Long logId, Model model, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/login";

        // 작성자 + 상태 검증 포함된 헬퍼
        StatusLogDto dto = statusLogService.getEditableLog(logId, loginUser.getId());
        PlaceDetailsDto place = placeService.getPlaceDetails(dto.getPlaceId());

        model.addAttribute("statusLog", dto);
        model.addAttribute("place", place);

        return "place/edit";
    }

    // ─────────────────────────────────────────────
    // [3] 지도 관련 페이지
    // ─────────────────────────────────────────────

    /**
     * [Check]
     * [3-1] 주변 졍보 확인 (답변 달린 내용)
     * page: index.jsp
     */
    @GetMapping("/nearby")
    public String nearbyPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "map/nearby";
    }

    /**
     * [3-2] 주변 요청 확인 (답변할 요청)
     * page: index.jsp
     */
    @GetMapping("/nearby/request-list")
    public String nearbyRequestListPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "map/request-list";
    }

    // ─────────────────────────────────────────────
    // [4] 장소 관련 페이지
    // ─────────────────────────────────────────────

    /**
     * [4-1] 장소 커뮤니티 이동
     * page: request/detail.jsp
     * page: place/page-search.jsp
     * - 해당 장소의 커뮤니티 페이지로 이동
     */
    @GetMapping("/place/community/{placeId}")
    public String showCommunityPage(@PathVariable Long placeId,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        PlaceDetailsDto place = placeService.getPlaceDetails(placeId);
        PageResult<StatusLogDto> pagedNotices = statusLogService.getPagedRegisterLogsByPlace(placeId, page, 3); // 5개씩
        List<StatusLogDto> recentLogs = statusLogService.getLogsByPlace(placeId); // 3시간 이내
        StatusLogDto latestLog = statusLogService.getLatestRegisterLogByPlaceId(placeId); // 최근 장소 응답
        List<RequestDto> placeRequests = requestService.getRequestsByPlaceId(placeId);

        System.out.println("📡 recentLogs size: " + recentLogs.size());

        model.addAttribute("place", place);
        model.addAttribute("pagedNotices", pagedNotices);
        model.addAttribute("recentLogs", recentLogs);
        model.addAttribute("latestLog", latestLog);
        model.addAttribute("placeRequests", placeRequests);
        model.addAttribute("naverMapClientId", naverMapClientId);
        return "place/community";
    }

    /**
     * [4-2] 공식 장소 커뮤니티 검색 페이지
     * page: index.jsp
     */
    @GetMapping("/place/search")
    public String showPlaceSearchPage(Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);
        return "place/place-search"; // JSP 파일명
    }

    // ─────────────────────────────────────────────
    // [5] 관리자 페이지
    // ─────────────────────────────────────────────

    /**
     * [5-1] 관리자 페이지 이동
     * page: common/header.jsp
     */
    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        return isAdmin(session) ? "admin/admin" : "redirect:/login?error=unauthorized";
    }

    /**
     * [5-2] 관리자 통계보기 페이지
     * page: admin/admin.jsp
     */
    @GetMapping("/admin/stats")
    public String adminStatsPage(HttpSession session) {
        return isAdmin(session) ? "admin/stats" : "redirect:/login?error=unauthorized";
    }

    /**
     * [5-3] 관리자 사용자 관리 페이지
     * page: admin/admin.jsp
     */
    @GetMapping("/admin/users")
    public String adminUsersPage(HttpSession session) {
        return isAdmin(session) ? "admin/users" : "redirect:/login?error=unauthorized";
    }

    /**
     * [5-4] 관리자 신고관리 페이지
     * page: admin/admin.jsp
     */
    @GetMapping("/admin/reports")
    public String adminReportsPage(HttpSession session) {
        return isAdmin(session) ? "admin/reports" : "redirect:/login?error=unauthorized";
    }

    /**
     * [5-5] 관리자 로그 관리 페이지
     * page: admin/admin.jsp
     */
    @GetMapping("/admin/logs")
    public String adminLogPage(HttpSession session) {
        return isAdmin(session) ? "admin/logs" : "redirect:/login?error=unauthorized";
    }

    // ────────────────────────────────────────
    // [*] 내부 공통 메서드
    // ────────────────────────────────────────

    /**
     * [1] ADMIN CHECK 메서드
     * PageController: adminPage
     * PageController: adminStatsPage
     * PageController: adminUsersPage
     * PageController: adminReportsPage
     */
    private boolean isAdmin(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        return loginUser != null && "ADMIN".equals(loginUser.getRole());
    }
}
