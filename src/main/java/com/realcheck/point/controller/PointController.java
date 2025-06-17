package com.realcheck.point.controller;

import com.realcheck.point.service.PointService;
import com.realcheck.user.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PointController
 * - 사용자 포인트 관련 API 제공 (내역 조회 등)
 */
@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // ─────────────────────────────────────────────
    // [1] 사용자 포인트 내역 조회 API
    // ─────────────────────────────────────────────

    /**
     * [1-1] 내 포인트 조회 API
     * - 호출 페이지: user/mypage.jsp
     * - 로그인된 사용자의 포인트 지급 내역을 반환
     * - 페이지네이션 적용: page, size 파라미터 지원
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPointsPaged(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page, // 조회할 페이지 번호 (0부터 시작)
            @RequestParam(defaultValue = "10") int size // 한 페이지당 항목 수
    ) {
        // [1] 세션에서 로그인된 사용자 정보 조회
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다."); // 인증 실패 응답
        }

        // [2] 서비스 호출하여 해당 사용자의 포인트 내역 페이지 조회
        return ResponseEntity.ok(
                pointService.getPagedPointsByUserId(loginUser.getId(), page, size));
    }
}
