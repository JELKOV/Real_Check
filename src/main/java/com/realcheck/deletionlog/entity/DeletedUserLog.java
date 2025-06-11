package com.realcheck.deletionlog.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * DeletedUserLog 엔티티
 * - 실제 사용자 탈퇴 시 로그 기록용
 * - 월별 탈퇴 통계, 관리자 대시보드 통계 등에 활용
 */
@Entity
@Table(name = "deleted_user_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DeletedUserLog {

    // ─────────────────────────────────────────────
    // [1] 기본 정보
    // ─────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 탈퇴한 사용자 ID (User.id)
    @Column(nullable = false)
    private Long userId;

    // 탈퇴한 사용자 이메일
    @Column(nullable = false)
    private String email;

    // 탈퇴 시각
    @Column(nullable = false)
    private LocalDateTime deletedAt;

    /**
     * DeletedUserLog 생성자
     * - userId, email, deletedAt을 초기화
     *
     * @param userId   탈퇴한 사용자 ID
     * @param email    탈퇴한 사용자 이메일
     * @param deletedAt 탈퇴 시각
     */
    public DeletedUserLog(Long userId, String email, LocalDateTime deletedAt) {
        this.userId = userId;
        this.email = email;
        this.deletedAt = deletedAt;
    }
}
