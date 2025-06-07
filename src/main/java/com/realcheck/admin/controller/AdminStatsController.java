package com.realcheck.admin.controller;

import com.realcheck.admin.dto.CategoryLogCountDto;
import com.realcheck.admin.dto.MonthlyUserStatDto;
import com.realcheck.admin.dto.TopContributingUserDto;
import com.realcheck.admin.dto.TopReportedUserDto;
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
    // [1] 통계 API (총 신고 수, 총 포인트, 월별 로그 수)
    // ─────────────────────────────────────────────

    /**
     * [1-1] 전체 신고 수 반환 API
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
     * [1-2] 전체 발행된 포인트 총량 조회 API
     * page: admin/stats.jsp
     * - 현재까지 지급된 모든 포인트 총합
     * - (EARN, DEDUCT, RESERVE, REFUND 등)은 계산에서 제외
     */
    @GetMapping("/points")
    public ResponseEntity<?> getTotalPointSum(HttpSession session) {
        return ResponseEntity.ok(Map.of("pointTotal", adminStatsService.getTotalPointSum()));
    }

    /**
     * [1-3] 월별 상태 로그 등록 수 반환 API
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

    /**
     * [1-4] 카테고리별 상태로그 수
     * URL: GET /api/admin/stats/logs/category
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
     * [1-5] 월별 사용자 가입 통계
     * URL: GET /api/admin/stats/users/monthly
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
     * [1-6] 신고 Top 10 유저
     * URL: GET /api/admin/stats/reports/top-users
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
     * [1-7] 기여 Top 10 유저
     * URL: GET /api/admin/stats/users/top
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
}
