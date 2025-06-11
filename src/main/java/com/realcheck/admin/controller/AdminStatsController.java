package com.realcheck.admin.controller;

import com.realcheck.admin.dto.CategoryLogCountDto;
import com.realcheck.admin.dto.CategoryStatDto;
import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.admin.dto.MonthlyUserStatDto;
import com.realcheck.admin.dto.TopContributingUserDto;
import com.realcheck.admin.dto.TopReportedUserDto;
import com.realcheck.admin.dto.UserRequestStatDto;
import com.realcheck.admin.service.AdminStatsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AdminStatsController
 * - 관리자 전용 통계 API를 제공하는 컨트롤러
 * - 전체 신고 수, 포인트 총합, 월별 등록 수 등 운영 데이터 조회에 사용
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    // ─────────────────────────────────────────────
    // [1] 통계 - 장소 관련
    // ─────────────────────────────────────────────

    /**
     * [1-1] 장소 상태별 등록 수
     * page: admin/stats.jsp
     */
    @GetMapping("/places/status")
    public ResponseEntity<?> getPlaceStatusCounts() {
        return ResponseEntity.ok(adminStatsService.getPlaceStatusCounts());
    }

    /**
     * [1-2] 월별 장소 등록 수
     * page: admin/stats.jsp
     */
    @GetMapping("/places/monthly")
    public ResponseEntity<List<MonthlyStatDto>> getMonthlyPlaceRegistrations() {
        return ResponseEntity.ok(adminStatsService.getMonthlyPlaceRegistrations());
    }

    // ─────────────────────────────────────────────
    // [2] 통계 - 로그 관련
    // ─────────────────────────────────────────────

    /**
     * [2-1] 전체 신고 수 반환 API
     * page: admin/stats.jsp
     * - 시스템 내 누적 신고 건수를 반환
     * - 예: { "reportCount": 17 }
     */
    @GetMapping("/reports")
    public ResponseEntity<?> getTotalReportCount(HttpSession session) {
        // 관리자 인증 생략 (공통 모듈화 가능)
        return ResponseEntity.ok(Map.of("reportCount", adminStatsService.getTotalReportCount()));
    }

    /**
     * [2-2] 총 답변 수
     * page: admin/stats.jsp
     * Response: [
     * { "category": "ANSWER", "count": 52 },
     * { "category": "REGISTER", "count": 37 },
     * { "category": "FREE_SHARE", "count": 19 }
     * ]
     */
    @GetMapping("/logs/category")
    public ResponseEntity<List<CategoryLogCountDto>> getCategoryLogStats(HttpSession session) {
        List<CategoryLogCountDto> result = adminStatsService.getLogCountByCategory();
        return ResponseEntity.ok(result);
    }

    /**
     * [2-3] 월별 상태 로그 등록 수 반환 API
     * page: admin/stats.jsp
     * - 월별로 등록된 StatusLog 수를 집계해서 리턴
     * - 프론트 대시보드에서 차트로 활용 가능
     * - 예:
     * [
     * { "year": 2025, "month": 4, "count": 37 },
     * { "year": 2025, "month": 3, "count": 12 }
     * ]
     */
    @GetMapping("/logs/monthly")
    public ResponseEntity<?> getMonthlyStatusLogStats(HttpSession session) {
        return ResponseEntity.ok(adminStatsService.getMonthlyStatusLogCount());
    }

    // ─────────────────────────────────────────────
    // [3] 통계 - 요청 관련
    // ─────────────────────────────────────────────

    /**
     * [3-1] 월별 요청 등록 수 통계
     * page: admin/stats.jsp
     * - 요청 생성 시점을 기준으로 월별 등록 수 반환
     */
    @GetMapping("/monthly")
    public List<MonthlyStatDto> getMonthlyRequestStats() {
        return adminStatsService.getMonthlyRequestRegistrations();
    }

    /**
     * [3-2] 카테고리별 요청 수 통계
     * page: admin/stats.jsp
     * - 각 카테고리별 요청 수를 집계하여 반환
     */
    @GetMapping("/category")
    public List<CategoryStatDto> getCategoryStats() {
        return adminStatsService.getRequestCategoryStats();
    }

    /**
     * [3-3] 요청 상태 통계 (Open / Closed)
     * page: admin/stats.jsp
     */
    @GetMapping("/status")
    public Map<String, Long> getRequestStatusStats() {
        return adminStatsService.getRequestStatusStats();
    }

    /**
     * [3-4] 요청 Top 사용자 통계 (요청을 가장 많이 한 사용자 10명)
     * page: admin/stats.jsp
     */
    @GetMapping("/top-users")
    public List<UserRequestStatDto> getTopUsers() {
        return adminStatsService.getTopRequestUsers();
    }

    // ─────────────────────────────────────────────
    // [4] 통계 - 유저 관련
    // ─────────────────────────────────────────────

    /**
     * [4-1] 월별 사용자 가입 통계
     * page: admin/stats.jsp
     * Response: [
     * { "year": 2025, "month": 4, "signUpCount": 42 },
     * { "year": 2025, "month": 3, "signUpCount": 28 }
     * ]
     */
    @GetMapping("/users/monthly")
    public ResponseEntity<List<MonthlyUserStatDto>> getMonthlyUserStats(HttpSession session) {
        List<MonthlyUserStatDto> result = adminStatsService.getMonthlyUserSignUpStats();
        return ResponseEntity.ok(result);
    }

    /**
     * [4-2] 월별 탈퇴 사용자 통계
     * page: admin/stats.jsp
     * - 월별로 탈퇴한 사용자 수를 집계하여 반환
     */
    @GetMapping("/users/deleted/monthly")
    public ResponseEntity<List<MonthlyUserStatDto>> getMonthlyDeletedUsers() {
        return ResponseEntity.ok(adminStatsService.getMonthlyUserDeletionStats());
    }

    /**
     * [4-3] 신고 Top 10 유저
     * page: admin/stats.jsp
     * Response: [
     * { "userId": 5, "nickname": "alice", "reportCount": 10 },
     * { "userId": 12, "nickname": "bob", "reportCount": 8 },
     * ...
     * ]
     */
    @GetMapping("/reports/top-users")
    public ResponseEntity<List<TopReportedUserDto>> getTopReportedUsers(HttpSession session) {
        List<TopReportedUserDto> result = adminStatsService.getTopReportedUsers();
        return ResponseEntity.ok(result);
    }

    /**
     * [4-4] 기여 Top 10 유저
     * page: admin/stats.jsp
     * Response: [
     * { "userId": 8, "nickname": "charlie", "logCount": 42 },
     * { "userId": 3, "nickname": "david", "logCount": 36 },
     * ...
     * ]
     */
    @GetMapping("/users/top")
    public ResponseEntity<List<TopContributingUserDto>> getTopContributors(HttpSession session) {
        List<TopContributingUserDto> result = adminStatsService.getTopContributingUsers();
        return ResponseEntity.ok(result);
    }

    /**
     * [4-5] 현재 활성/비활성 사용자 수 반환
     * page: admin/stats.jsp
     * - 활성화된(isActive = true) 사용자 수와 비활성화된(isActive = false) 사용자 수를 반환
     */
    @GetMapping("/users/active-stats")
    public ResponseEntity<Map<String, Long>> getUserActiveStats() {
        return ResponseEntity.ok(adminStatsService.getUserActiveStats());
    }

    // ─────────────────────────────────────────────
    // [5] 통계 - 포인트 관련
    // ─────────────────────────────────────────────

    /**
     * [5-1] 전체 발행된 포인트 총량 조회 API
     * page: admin/stats.jsp
     * - 현재까지 지급된 모든 포인트 총합
     * - (EARN, DEDUCT, RESERVE, REFUND 등)은 계산에서 제외
     */
    @GetMapping("/points")
    public ResponseEntity<?> getTotalPointSum(HttpSession session) {
        return ResponseEntity.ok(Map.of("pointTotal", adminStatsService.getTotalPointSum()));
    }

}