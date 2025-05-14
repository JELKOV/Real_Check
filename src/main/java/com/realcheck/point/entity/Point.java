package com.realcheck.point.entity;

import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int amount; // 지급/차감 포인트 (양수: 지급, 음수: 차감)
    private String reason; // 지급/차감 사유
    private LocalDateTime earnedAt; // 지급/차감 시점

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type; // EARN, DEDUCT, REWARD 등 포인트 유형
}