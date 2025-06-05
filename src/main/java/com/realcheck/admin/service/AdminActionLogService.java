package com.realcheck.admin.service;

import com.realcheck.admin.dto.AdminActionLogDto;
import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.AdminActionLog;
import com.realcheck.admin.entity.TargetType;
import com.realcheck.admin.repository.AdminActionLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * AdminActionLogService
 * - 관리자 행동 로그의 생성 및 조회 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;

    // ─────────────────────────────────────────────
    // [1] 생성 기능
    // ─────────────────────────────────────────────

    /**
     * [1-1] 관리자 행동 로그 저장
     * UserAdminSerivce:unblockUser
     * UserAdminSerivce:blockUser
     * - 관리자 ID, 대상 ID, 액션 타입, 대상 타입, 설명을 받아 로그를 생성
     * - 예: BLOCK, UNBLOCK, APPROVE 등
     */
    public void saveLog(Long adminId, Long targetId, ActionType actionType, TargetType targetType,
            String description) {
        AdminActionLog log = AdminActionLog.of(adminId, targetId, actionType, targetType, description);
        adminActionLogRepository.save(log);
    }

    // ─────────────────────────────────────────────
    // [2] 조회 기능
    // ─────────────────────────────────────────────

    /**
     * [2-1] 관리자 로그 조회
     * AdminActionLogController:getLogs
     * - 조건 조합에 따라 로그를 필터링하여 조회
     * - 지원 필터 조합
     * - adminId + actionType + targetType
     * - adminId + actionType
     * - adminId + targetType
     * - actionType + targetType
     * - adminId
     * - targetId + targetType
     * - actionType
     * - 모든 로그 조회
     */
    public Page<AdminActionLogDto> getLogs(
            Long adminId,
            ActionType actionType,
            TargetType targetType,
            Long targetId,
            Pageable pageable) {

        Page<AdminActionLog> logs;

        // [1] 관리자 ID + 대상 유형 + 작업 유형이 모두 지정된 경우
        if (adminId != null && actionType != null && targetType != null) {
            logs = adminActionLogRepository.findByAdminIdAndTargetTypeAndActionType(
                    adminId, targetType, actionType, pageable);
            // [2] 관리자 ID + 작업 유형만 지정된 경우
        } else if (adminId != null && actionType != null) {
            logs = adminActionLogRepository.findByAdminIdAndActionType(
                    adminId, actionType, pageable);
            // [3] 관리자 ID + 대상 유형만 지정된 경우
        } else if (adminId != null && targetType != null) {
            logs = adminActionLogRepository.findByAdminIdAndTargetType(
                    adminId, targetType, pageable);
            // [4] 대상 유형 + 작업 유형만 지정된 경우
        } else if (actionType != null && targetType != null) {
            logs = adminActionLogRepository.findByTargetTypeAndActionType(
                    targetType, actionType, pageable);
            // [5] 관리자 ID만 지정된 경우
        } else if (adminId != null) {
            logs = adminActionLogRepository.findByAdminId(adminId, pageable);
            // [6] 특정 대상 ID와 대상 유형이 지정된 경우 (예: 사용자 ID = 7, 대상 = USER)
        } else if (targetId != null && targetType != null) {
            logs = adminActionLogRepository.findByTargetIdAndTargetType(targetId, targetType, pageable);
            // [7] 작업 유형만 지정된 경우
        } else if (actionType != null) {
            logs = adminActionLogRepository.findByActionType(actionType, pageable);
            // [8] 필터 조건이 전혀 없는 경우 - 전체 로그 조회
        } else {
            logs = adminActionLogRepository.findAll(pageable);
        }

        return logs.map(AdminActionLogDto::fromEntity);
    }
}
