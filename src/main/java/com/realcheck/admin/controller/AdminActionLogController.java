package com.realcheck.admin.controller;

import com.realcheck.admin.dto.AdminActionLogDto;
import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.TargetType;
import com.realcheck.admin.service.AdminActionLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AdminActionLogController
 * - 관리자 행동 로그 관련 API를 제공하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminActionLogController {

    private final AdminActionLogService adminActionLogService;

    /**
     * [1] 관리자 행동 로그 조회 API
     * page: admin/logs.jsp
     * - 다양한 조건으로 필터링된 로그 목록을 조회
     * - 조건: adminId, actionType, targetType, targetId
     * - 페이징 지원 (page, size)
     */
    @GetMapping
    public ResponseEntity<Page<AdminActionLogDto>> getLogs(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) TargetType targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<AdminActionLogDto> result = adminActionLogService.getLogs(adminId, actionType, targetType, targetId, pageable);
        return ResponseEntity.ok(result);
    }
}
