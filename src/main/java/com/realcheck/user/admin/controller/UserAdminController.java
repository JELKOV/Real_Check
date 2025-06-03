package com.realcheck.user.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.admin.service.UserAdminService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * UserAdminController
 * - 관리자 전용 사용자 관리 컨트롤러
 * - 차단 사용자 해제, 사용자 검색, 차단 사용자 목록 조회 API 제공
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final StatusLogService statusLogService;

    // ─────────────────────────────────────────────
    // [1] 사용자 검색 기능 (이메일 / 닉네임 기반)
    // ─────────────────────────────────────────────

    /**
     * [1-1] 사용자 검색 API
     * page: admin/users.jsp
     * - 이메일 또는 닉네임에 키워드가 포함된 사용자 리스트 반환
     * - 관리자 화면에서 검색 기능에 사용
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(userAdminService.searchUsers(keyword));
    }

    /**
     * [1-2] 사용자 상세 정보 조회 API
     * page: admin/users.jsp
     * - 특정 사용자 ID의 상세 정보와 해당 사용자의 로그 수 반환
     * - 관리자 권한 확인 후에만 접근 가능
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }

        UserDto userDto = userAdminService.getUserDto(id);
        long totalLogs = statusLogService.countLogsByUserId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("user", userDto);
        response.put("totalLogs", totalLogs);

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // [2] 차단 사용자 관리 기능
    // ─────────────────────────────────────────────

    /**
     * [2-1] 차단된 사용자 목록 조회 API
     * page: admin/users.jsp
     * - 로그인된 관리자인지 확인 후, isActive = false인 사용자 리스트 반환
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

    /**
     * [2-2] 사용자 차단 해제 API
     * page: admin/users.jsp
     * - 특정 사용자 ID의 계정을 다시 활성화함 (isActive = true)
     */
    @PatchMapping("/{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        userAdminService.unblockUser(userId);
        return ResponseEntity.ok("차단 해제 완료");
    }

    /**
     * [미사용]
     * [2-3] 신고 누적 사용자 목록 (차단되지 않았지만 신고된 사용자)
     */
    @GetMapping("/reported")
    public ResponseEntity<List<UserDto>> getReportedUsers() {
        return ResponseEntity.ok(userAdminService.getReportedUsers());
    }

}
