package com.realcheck.admin.controller;

import com.realcheck.admin.service.AdminStatsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * page: admin/stats.jsp
     * [1] 전체 신고 수 반환 API
     * - 시스템 내 누적 신고 건수를 반환
     * - 예: { "reportCount": 17 }
     */
    @GetMapping("/reports")
    public ResponseEntity<?> getTotalReportCount(HttpSession session) {
        // 관리자 인증 생략 (공통 모듈화 가능)
        return ResponseEntity.ok(Map.of("reportCount", adminStatsService.getTotalReportCount()));
    }

    /**
     * page: admin/stats.jsp
     * [2] 전체 포인트 합계 반환 API
     * - 현재까지 지급된 모든 포인트 총합
     * - 예: { "pointTotal": 1040 }
     */
    @GetMapping("/points")
    public ResponseEntity<?> getTotalPointSum(HttpSession session) {
        return ResponseEntity.ok(Map.of("pointTotal", adminStatsService.getTotalPointSum()));
    }

    /**
     * page: admin/stats.jsp
     * [3] 월별 상태 로그 등록 수 반환 API
     * - 월별로 등록된 StatusLog 수를 집계해서 리턴
     * - 프론트 대시보드에서 차트로 활용 가능
     *
     * 예: 
     * [
     *   { "year": 2025, "month": 4, "count": 37 },
     *   { "year": 2025, "month": 3, "count": 12 }
     * ]
     */
    @GetMapping("/logs/monthly")
    public ResponseEntity<?> getMonthlyStatusLogStats(HttpSession session) {
        return ResponseEntity.ok(adminStatsService.getMonthlyStatusLogCount());
    }
}
