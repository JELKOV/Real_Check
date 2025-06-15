package com.realcheck.admin.repository;

import com.realcheck.admin.entity.AdminActionLog;
import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.TargetType;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AdminActionLogRepository
 * - 관리자 행동 로그를 위한 JPA 리포지토리
 * - 다양한 필터 조건으로 조회 가능 (관리자 ID, 대상 ID/타입, 액션 타입 등)
 */
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    /**
     * [1] 특정 관리자 ID로 필터링하여 로그 조회 (페이징 포함)
     * AdminActionLogService:getLogs
     * - 관리자 ID로 필터링된 로그를 페이징 처리하여 조회
     */
    Page<AdminActionLog> findByAdminId(Long adminId, Pageable pageable);

    /**
     * [2] 대상 ID + 대상 타입으로 로그 조회
     * AdminActionLogService:getLogs
     * - 특정 대상에 대한 액션 로그를 조회할 때 사용
     */
    Page<AdminActionLog> findByTargetIdAndTargetType(Long targetId, TargetType targetType, Pageable pageable);

    /**
     * [3] 액션 타입으로 로그 조회
     * AdminActionLogService:getLogs
     * - 특정 액션 타입에 해당하는 로그를 조회
     * * 예: BLOCK, UNBLOCK, APPROVE 등
     * - 페이징 처리 지원
     */
    Page<AdminActionLog> findByActionType(ActionType actionType, Pageable pageable);

    /**
     * [4] 관리자 ID + 대상 타입 + 작업 유형을 모두 만족하는 로그 조회
     * AdminActionLogService:getLogs
     * - 특정 관리자에 의해 수행된 특정 액션 로그를 조회
     * - 페이징 처리 지원
     */
    Page<AdminActionLog> findByAdminIdAndTargetTypeAndActionType(
            Long adminId,
            TargetType targetType,
            ActionType actionType,
            Pageable pageable);

    /**
     * [5] 관리자 ID + 작업 유형
     * AdminActionLogService:getLogs
     * - 특정 관리자가 수행한 특정 작업유형(BLOCK 등) 로그 조회
     */
    Page<AdminActionLog> findByAdminIdAndActionType(Long adminId, ActionType actionType, Pageable pageable);

    /**
     * [6] 대상 타입 + 작업 유형
     * AdminActionLogService:getLogs
     * - 예: 모든 USER 대상에 대해 수행된 BLOCK 작업 로그 조회
     * - 관리자는 불문
     */
    Page<AdminActionLog> findByTargetTypeAndActionType(TargetType targetType, ActionType actionType, Pageable pageable);

    /**
     * [7] 대상 타입 + 허용된 작업 유형 목록으로 필터링하여 조회
     * AdminActionLogService:getLogs
     * - targetType만 지정하고 actionType은 전체일 때 사용됨
     * - 예: targetType=USER인 경우 BLOCK, UNBLOCK만 조회
     */
    Page<AdminActionLog> findByTargetTypeAndActionTypeIn(TargetType targetType, List<ActionType> actionTypes,
            Pageable pageable);

    /**
     * [8] 관리자 ID + 대상 타입 + 허용된 작업 유형 목록으로 조회
     * AdminActionLogService:getLogs
     * - adminId, targetType은 지정되었고 actionType은 전체일 때 사용
     * - 예: 관리자가 USER 대상에게 수행한 BLOCK, UNBLOCK만 조회
     */
    Page<AdminActionLog> findByAdminIdAndTargetTypeAndActionTypeIn(Long adminId, TargetType targetType,
            List<ActionType> actionTypes, Pageable pageable);
}
