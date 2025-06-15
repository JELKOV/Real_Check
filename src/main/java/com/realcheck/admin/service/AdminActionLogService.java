package com.realcheck.admin.service;

import com.realcheck.admin.dto.AdminActionLogDto;
import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.AdminActionLog;
import com.realcheck.admin.entity.TargetType;
import com.realcheck.admin.repository.AdminActionLogRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

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
     * UserAdminSerivce: unblockUser
     * UserAdminSerivce: blockUser
     * ReportAdminService: deleteReport
     * AdminPlaceService: approvePlace
     * AdminPlaceService: rejectPlace
     * StautsLogAdminService: blockLog
     * StatusLogAdminService: unblockLog
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
            // [3] adminId + targetType (ActionType 생략) → 제한된 타입만
        } else if (adminId != null && targetType != null && actionType == null) {
            List<ActionType> allowed = getAllowedActionsByTarget(targetType);
            logs = adminActionLogRepository.findByAdminIdAndTargetTypeAndActionTypeIn(
                    adminId, targetType, allowed, pageable);
            // [4] 대상 유형 + 작업 유형만 지정된 경우
        } else if (targetType != null && actionType != null) {
            logs = adminActionLogRepository.findByTargetTypeAndActionType(
                    targetType, actionType, pageable);
            // [5] targetType만 설정 → 대상에 맞는 ActionType 목록 제한 검색
        } else if (targetType != null && actionType == null) {
            List<ActionType> allowed = getAllowedActionsByTarget(targetType);
            logs = adminActionLogRepository.findByTargetTypeAndActionTypeIn(
                    targetType, allowed, pageable);
            // [6] 관리자 ID만 지정된 경우
        } else if (adminId != null) {
            logs = adminActionLogRepository.findByAdminId(adminId, pageable);
            // [7] 특정 대상 ID와 대상 유형이 지정된 경우 (예: 사용자 ID = 7, 대상 = USER)
        } else if (targetId != null && targetType != null) {
            logs = adminActionLogRepository.findByTargetIdAndTargetType(targetId, targetType, pageable);
            // [8] 작업 유형만 지정된 경우
        } else if (actionType != null) {
            logs = adminActionLogRepository.findByActionType(actionType, pageable);
            // [9] 필터 조건이 전혀 없는 경우 - 전체 로그 조회
        } else {
            logs = adminActionLogRepository.findAll(pageable);
        }

        return logs.map(AdminActionLogDto::fromEntity);
    }

    // ─────────────────────────────────────────────
    // [3] 내부 메서드
    // ─────────────────────────────────────────────

    /**
     * [3-1] 대상 유형(TargetType)에 따라 허용된 작업 유형(ActionType) 목록을 반환
     * AdminActionLogService: getlogs
     * - 조회 조건 중 actionType이 명시되지 않은 경우 (프론트에서 '전체' 선택 시),
     * - 해당 대상에 대해 실제 허용되는 작업 유형만 필터링하기 위해 사용됨
     * - 예: targetType=USER 일 때 BLOCK, UNBLOCK 만 필터링
     *
     * @param targetType 로그 대상의 타입 (USER, PLACE, REPORT, STATUS_LOG 등)
     * @return 해당 대상에 대해 허용되는 작업 유형(ActionType) 목록
     */
    private List<ActionType> getAllowedActionsByTarget(TargetType targetType) {
        return switch (targetType) {
            case USER -> List.of(ActionType.BLOCK, ActionType.UNBLOCK);
            case PLACE -> List.of(ActionType.APPROVE, ActionType.REJECT);
            case REPORT -> List.of(ActionType.REJECT);
            case STATUS_LOG -> List.of(ActionType.BLOCK, ActionType.UNBLOCK);
        };
    }
}
