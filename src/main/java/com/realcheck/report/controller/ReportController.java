package com.realcheck.report.controller;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.service.ReportService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ReportController
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
     * - 로그인한 사용자가 상태 로그(StatusLog)를 신고할 수 있도록 처리
     * - 신고 내용에는 대상 로그 ID와 사유가 포함됨
     * - 로그인하지 않은 경우 401 응답 반환
     *
     * @param dto     신고 내용 (신고 사유, 신고할 StatusLog ID)
     * @param session 현재 로그인된 사용자 세션 정보
     * @return 신고 성공 메시지 or 인증 필요 응답
     */
    @PostMapping
    public ResponseEntity<String> report(@RequestBody ReportDto dto, HttpSession session) {
        // 1. 로그인 여부 확인
        UserDto loginUser = (UserDto) session.getAttribute("loginUser"); // 세션에서 로그인 정보를 불러옴
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다."); // 로그인이 안되어 있으면 401 에러 응답
        }

        // 2. 신고 처리 위임 (신고자 ID + 신고 대상 로그 정보)
        reportService.report(loginUser.getId(), dto); // 신고 로직 실행

        // 3. 성공 메시지 반환
        return ResponseEntity.ok("신고가 접수되었습니다."); // 성공 응답답
    }
}
