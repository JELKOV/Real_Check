package com.realcheck.admin.entity;

import java.time.LocalDateTime;

import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AdminActionLog
 * - 관리자가 수행한 주요 행동(차단, 승인 등)을 기록하는 로그 엔티티
 * - 관리자 식별자, 대상 ID, 작업 유형, 설명, 생성 시점 등을 저장함
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admin_action_logs")
public class AdminActionLog {

    // ─────────────────────────────────────────────
    // [1] 기본 필드
    // ─────────────────────────────────────────────

    /** 로그의 고유 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 관리자 ID (users 테이블의 id 값을 저장) */
    @Column(name = "admin_id")
    private Long adminId;

    /** 조치 대상 ID (예: 사용자, 장소 등 해당 대상의 ID) */
    private Long targetId;

    /** 수행한 작업 유형 (BLOCK, UNBLOCK, APPROVE, REJECT 등) */
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    /** 작업 대상의 종류 (USER, PLACE, REPORT, STATUS_LOG 등) */
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    /** 작업에 대한 설명 또는 사유 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 로그 생성 시점 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─────────────────────────────────────────────
    // [2] 생성 시 자동으로 현재 시간 기록
    // ─────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────
    // [3] db와의 연관 관계 설정
    // ─────────────────────────────────────────────

    /**
     * 관리자 엔티티와의 연관 관계 (다대일)
     * - adminId 외래키를 통해 User 엔티티와 연결됨
     * - insertable=false, updatable=false → adminId 필드를 통해 값이 세팅되며, 해당 연관 필드는 읽기 전용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", insertable = false, updatable = false)
    private User admin;

    // ─────────────────────────────────────────────
    // [4] 정적 팩토리 메서드
    // ─────────────────────────────────────────────

    /**
     * AdminActionLog 객체를 생성하는 정적 메서드
     * 
     * @param adminId     관리자 ID
     * @param targetId    대상 ID
     * @param actionType  작업 유형
     * @param targetType  대상 유형
     * @param description 작업 설명
     * @return AdminActionLog 객체
     */
    public static AdminActionLog of(Long adminId, Long targetId, ActionType actionType, TargetType targetType,
            String description) {
        AdminActionLog log = new AdminActionLog();
        log.setAdminId(adminId);
        log.setTargetId(targetId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setDescription(description);
        return log;
    }
}
