package com.realcheck.admin.controller;

import com.realcheck.admin.service.StatusLogAdminService;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.user.dto.UserDto;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * StatusLogAdminController
 * - 관리자용 자발 공유(StatusType.FREE_SHARE) 로그 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/status-logs")
@RequiredArgsConstructor
public class StatusLogAdminController {

    private final StatusLogAdminService statusLogAdminService;

    /**
     * [1] 자발적 공유 로그 목록 조회 (관리자 전용)
     * page: admin/status-logs.jsp
     */
    @GetMapping
    public ResponseEntity<Page<StatusLogDto>> getFreeShareLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(statusLogAdminService.getFreeShareLogs(PageRequest.of(page, size)));
    }

    /**
     * [2] FREE_SHARE 로그 차단
     * page: admin/status-logs.jsp
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockLog(@PathVariable Long id, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build();
        }

        statusLogAdminService.blockLog(id, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * [3] FREE_SHARE 로그 차단 해제
     * page: admin/status-logs.jsp
     */
    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblockLog(@PathVariable Long id, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build();
        }

        statusLogAdminService.unblockLog(id, loginUser.getId());
        return ResponseEntity.ok().build();
    }
}
