package com.realcheck.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realcheck.admin.service.ReportAdminService;
import com.realcheck.report.dto.ReportDto;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * ReportAdminController (ALL DONE)
 * - 관리자 전용 신고 기능 컨트롤러
 * - 전체 신고 목록 조회, 신고 수 통계 제공
 */
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class ReportAdminController {

    // 신고 관련 관리자 서비스 의존성 주입
    private final ReportAdminService reportAdminService;

    /**
     * [1] 전체 신고 내역 조회 API
     * page: admin/reports.jsp
     * - 관리자만 접근 가능
     * - 세션에서 ADMIN 권한 확인 후 전체 신고 리스트 반환
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

    /**
     * [2] 특정 상태 로그(StatusLog)에 대한 신고 횟수 조회 API
     * page: admin/reports.jsp
     * - 관리자만 접근 가능
     * - statusLogId를 기준으로 신고 수 반환
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

    /**
     * [3] 신고된 상태 로그 목록 조회 (숨김 상태)
     * page: admin/reports.jsp
     * - 관리자만 접근 가능
     * - 숨김 처리된 상태 로그만 조회하여 반환
     */
    @GetMapping("/logs")
    public ResponseEntity<List<StatusLogDto>> getReportedLogs() {
        List<StatusLogDto> reportedLogs = reportAdminService.getHiddenLogs();
        return ResponseEntity.ok(reportedLogs);
    }

    /**
     * [4] 특정 StatusLog에 속한 신고 상세 목록 조회
     * page: admin/reports.jsp
     * - 관리자만 접근 가능
     * - statusLogId를 기준으로 해당 상태 로그에 대한 모든 신고 목록 반환
     * - 이 API는 관리자가 신고된 상태 로그의 상세 정보를 확인할 때 사용됩니다.
     * - 예를 들어, 관리자가 특정 상태 로그에 대해 "🔎 상세" 버튼을 클릭했을 때 호출됩니다.
     */
    @GetMapping("/statuslog/{statusLogId}/reports")
    public ResponseEntity<List<ReportDto>> getReportsForStatusLog(
            @PathVariable Long statusLogId,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build();
        }
        List<ReportDto> reports = reportAdminService.getReportsForStatusLog(statusLogId);
        return ResponseEntity.ok(reports);
    }

    /**
     * [5] 오탐(False-Positive) 신고 수동 삭제
     * page: admin/reports.jsp
     * - 관리자만 접근 가능
     * - reportId를 기준으로 신고 삭제
     * - 이 API는 관리자가 오탐 신고를 수동으로 삭제할 때 사용됩니다.
     * - 예를 들어, 관리자가 신고된 상태 로그에 대해 "삭제" 버튼을 클릭했을 때 호출됩니다.
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReportByAdmin(
            @PathVariable Long reportId,
            HttpSession session) {

        // 1) 관리자 권한 체크
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }

        try {
            reportAdminService.deleteReport(reportId, loginUser.getId());
        } catch (IllegalArgumentException e) {
            // reportId가 없거나, 이미 삭제된 경우 등
            return ResponseEntity.status(404).body(e.getMessage());
        }

        return ResponseEntity.ok().body("신고(ID=" + reportId + ")를 삭제했습니다.");
    }

}
