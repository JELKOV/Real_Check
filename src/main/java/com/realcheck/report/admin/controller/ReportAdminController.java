package com.realcheck.report.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realcheck.report.admin.service.ReportAdminService;
import com.realcheck.report.dto.ReportDto;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * ReportAdminController
 * - 관리자 전용 신고 기능 컨트롤러
 * - 전체 신고 목록 조회, 신고 수 통계 제공
 */
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class ReportAdminController {

    // 신고 관련 관리자 서비스 의존성 주입
    private final ReportAdminService reportAdminService;

    // ─────────────────────────────────────────────
    // [1] 전체 신고 목록 조회
    // ─────────────────────────────────────────────

    /**
     * [1-1] 전체 신고 내역 조회 API
     * - 관리자만 접근 가능
     * - 세션에서 ADMIN 권한 확인 후 전체 신고 리스트 반환
     *
     * @param session 현재 로그인된 사용자 세션
     * @return 신고 내역 리스트 (List<ReportDto>)
     */
    @GetMapping("/all")
    public ResponseEntity<List<ReportDto>> getAllReports(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 및 관리자 권한 체크
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build(); // 권한 없음
        }

        // 전체 신고 리스트 반환
        return ResponseEntity.ok(reportAdminService.getAllReports());
    }

    // ─────────────────────────────────────────────
    // [2] 특정 상태 로그에 대한 신고 수 조회
    // ─────────────────────────────────────────────

    /**
     * [2] 특정 상태 로그(StatusLog)에 대한 신고 횟수 조회 API
     * - 관리자만 접근 가능
     * - statusLogId를 기준으로 신고 수 반환
     *
     * @param statusLogId 조회할 대상 로그 ID
     * @param session 로그인된 사용자 세션
     * @return 신고 수 (예: {"count": 3})
     */
    @GetMapping("/count")
    public ResponseEntity<?> countReports(@RequestParam Long statusLogId, HttpSession session) {
        // <?> 리턴타입이 고정되어 있지 않음

        // 로그인 및 관리자 권한 확인
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }

        // 서비스 계층에 위임 → 해당 상태 로그에 대한 신고 횟수 반환
        long count = reportAdminService.countReportsByStatusLogId(statusLogId);
        // 결과를 Map 형태로 응답 (자동으로 JSON 변환됨)
        return ResponseEntity.ok().body(Map.of("count", count)); // {"count": 3}
    }
}
