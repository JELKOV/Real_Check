package com.realcheck.user.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realcheck.user.admin.service.UserAdminService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 사용자 관련 컨트롤러
 * - 비활성화(차단된) 사용자 목록 조회 등 관리자 기능 처리
 */
@RestController // REST API 전용 컨트롤러 (JSON 형태로 응답)
@RequestMapping("/api/admin/users") // 이 컨트롤러의 기본 URI 경로
@RequiredArgsConstructor // 생성자 주입을 Lombok이 자동 생성 (userAdminService를 주입)
public class UserAdminController {

    private final UserAdminService userAdminService; // 사용자 관리 서비스 (관리자 전용)

    /**
     * 비활성화된(차단된) 사용자 목록을 조회하는 관리자 전용 API
     * - 로그인된 관리자인지 확인 후, 차단된 사용자 목록 반환
     *
     * @param session 현재 로그인된 사용자 정보가 담긴 세션 객체
     * @return ResponseEntity (HTTP 200 OK + 비활성 사용자 리스트 or HTTP 403 오류)
     */
    @GetMapping("/blocked")
    public ResponseEntity<?> getBlockedUsers(HttpSession session) {
        // 로그인된 사용자 정보 가져오기
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인되지 않았거나 관리자 권한이 아닐 경우 403 Forbidden 응답
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }

        // 차단된 사용자 목록 조회 후 응답
        return ResponseEntity.ok(userAdminService.getBlockedUsers());
    }
}
