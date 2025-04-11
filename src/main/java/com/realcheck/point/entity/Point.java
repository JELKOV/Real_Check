package com.realcheck.point.entity;

import java.time.LocalDateTime;

import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자에게 지급된 포인트 내역을 저장하는 엔티티
 * - 포인트 적립 사유, 시점 등을 기록
 */
@Entity
@Table(name = "points") // 테이블명: points
@Getter
@Setter
@NoArgsConstructor
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 포인트 양 (예: 10, 20 등)
    private int amount;

    // 적립 사유 (예: "정보 제공", "랭킹 보상", "신고 인정")
    private String reason;

    // 포인트 획득 시각 – 기본값은 현재 시간
    private LocalDateTime earnedAt = LocalDateTime.now();

    // 포인트를 받은 사용자 정보
    @ManyToOne
    @JoinColumn(name = "user_id") // FK 이름은 user_id
    private User user;
}
