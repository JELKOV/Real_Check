package com.realcheck.point.entity;

import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (포인트 정보)
    // ─────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // (1) 지급/차감 금액 (양수: 적립, 음수: 차감)
    @Column(nullable = false)
    private int amount;

    // (2) 지급/차감 이유
    @Column(nullable = false)
    private String reason;

    // (3) 지급/차감 시점
    @Column(nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    /**
     * 포인트 타입 (Enum)
     * EARN: 적립, DEDUCT: 차감, REWARD: 보상 등
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PointType type;

    // ─────────────────────────────────────────────
    // [2] 연관 관계 (사용자)
    // ─────────────────────────────────────────────

    /**
     * 사용자 (포인트 지급/차감 대상)
     * N:1 관계 (여러 포인트 기록은 하나의 사용자와 연결)
     * Lazy 로딩: 실제로 접근할 때 사용자 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (this.earnedAt == null) {
            this.earnedAt = LocalDateTime.now();
        }
    }
}