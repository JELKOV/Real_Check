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
 * 포인트 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 내 포인트 내역 조회 API
    @GetMapping("/my")
    public ResponseEntity<List<PointDto>> getMyPoints(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).build(); // 로그인 필요
        }

        return ResponseEntity.ok(pointService.getPointsByUserId(loginUser.getId()));
    }
}
