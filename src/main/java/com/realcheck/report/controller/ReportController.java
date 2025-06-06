package com.realcheck.report.controller;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.service.ReportService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ReportController (ALL DONE)
 * - 사용자 신고 요청을 처리하는 REST 컨트롤러
 */
@RestController // REST API 전용 컨틀롤러임을 명시함
@RequestMapping("/api/report") // 요청 경로
@RequiredArgsConstructor // final 필드를 자동 생성자 주입입
public class ReportController {
    private final ReportService reportService;

    // ─────────────────────────────────────────────
    // [1] 신고 등록 API
    // ─────────────────────────────────────────────

    /**
     * [1-1] 신고 등록 API
     * page: user/detail.jsp
     * - 로그인한 사용자가 상태 로그(StatusLog)를 신고할 수 있도록 처리
     * - 신고 내용에는 대상 로그 ID와 사유가 포함됨
     * - 로그인하지 않은 경우 401 응답 반환
     */
    @PostMapping
    public ResponseEntity<String> report(@RequestBody ReportDto dto, HttpSession session) {
        // (1) 로그인 여부 확인
        UserDto loginUser = (UserDto) session.getAttribute("loginUser"); // 세션에서 로그인 정보를 불러옴
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다."); // 로그인이 안되어 있으면 401 에러 응답
        }

        // (2) 중복 신고 방지 (로그인 사용자 기준)
        boolean alreadyReported = reportService.hasAlreadyReported(loginUser.getId(), dto.getStatusLogId());
        if (alreadyReported) {
            return ResponseEntity.status(400).body("이미 신고한 상태입니다.");
        }

        // (3) 신고 처리
        reportService.report(loginUser.getId(), dto); // 신고 로직 실행
        return ResponseEntity.ok("신고가 접수되었습니다."); // 성공 응답
    }

    /**
     * [1-2] 신고 취소 처리
     * page: user/detail.jsp
     * - 로그인 사용자가 본인이 한 신고를 취소
     * - 신고 이력이 없거나 본인이 아닌 경우 400 응답
     */
    @DeleteMapping
    public ResponseEntity<String> cancelReport(@RequestParam Long statusLogId, HttpSession session) {
        // (1) 로그인 확인
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // (2) 신고 취소 로직 실행
        try {
            reportService.cancelReport(loginUser.getId(), statusLogId);
            return ResponseEntity.ok("신고가 취소되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * [1-3] 로그인 사용자가 특정 상태 로그에 신고했는지 확인
     * page: user/detail.jsp
     * - 프론트에서 UI 버튼 상태 결정 시 사용
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkReported(
            @RequestParam Long statusLogId,
            HttpSession session) {

        // (1) 로그인 여부 확인
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(false);
        }

        // (2) 해당 상태 로그에 대한 신고 여부 반환
        boolean isReported = reportService.hasAlreadyReported(loginUser.getId(), statusLogId);
        return ResponseEntity.ok(isReported);
    }
}
