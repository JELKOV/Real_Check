package com.realcheck.report.controller;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.service.ReportService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // REST API 전용 컨틀롤러임을 명시함
@RequestMapping("/api/report") // 요청 경로
@RequiredArgsConstructor // final 필드를 자동 생성자 주입입
public class ReportController {
    private final ReportService reportService;

    /**
     * 신고 등록 API
     * - 사용자가 상태 로그(StatusLog)를 신고할 수 있음
     * - 로그인된 사용자만 신고 가능
     * 
     * @param dto 신고내용(신고 사유, 상태 로그ID)
     * @param session 현재 로그인된 사용자 정보를 담고 있는 세션
     * @return 신고 완료 메시지 또는 인증 완료
     */

    @PostMapping
    public ResponseEntity<String> report(@RequestBody ReportDto dto, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser"); // 세션에서 로그인 정보를 불러옴
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다."); // 로그인이 안되어 있으면 401 에러 응답
        }

        reportService.report(loginUser.getId(), dto); // 신고 로직 실행
        return ResponseEntity.ok("신고가 접수되었습니다."); // 성공 응답답
    }
}
