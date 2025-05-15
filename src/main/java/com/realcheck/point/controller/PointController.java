package com.realcheck.point.controller;

import com.realcheck.point.dto.PointDto;
import com.realcheck.point.service.PointService;
import com.realcheck.user.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * [1-1] 내 포인트 조회 API [미사용]
     * 로그인된 사용자의 포인트 지급 내역을 반환
     * 로그인 정보는 세션에서 확인
     */
    @GetMapping("/my")
    public ResponseEntity<List<PointDto>> getMyPoints(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).build(); // 로그인 필요
        }

        return ResponseEntity.ok(pointService.getPointsByUserId(loginUser.getId()));
    }
}
