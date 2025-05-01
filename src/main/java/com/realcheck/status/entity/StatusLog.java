package com.realcheck.status.entity;

import java.time.LocalDateTime;

import com.realcheck.place.entity.Place;
import com.realcheck.request.entity.Request;
import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * StatusLog 엔티티
 * - 장소의 실시간 상태 정보 (대기 현황, 요청 답변, 자유 공유 등) 저장
 * - 요청과 연결되거나, 단독으로 등록될 수 있음
 */
@Entity
@Table(name = "status_logs")
@Getter
@Setter
@NoArgsConstructor
public class StatusLog {

    // ─────────────────────────────────────────────
    // 기본 필드
    // ─────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content; // 상태 설명 (예: "현재 대기 3명")
    private int waitCount; // 대기 인원 수
    private String imageUrl; // 이미지 경로 (선택적)

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 등록 시간 (기본값: 현재)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType statusType = StatusType.ANSWER; // 상태 타입 (ANSWER or FREE_SHARE)

    private boolean isSelected = false; // 답변 채택 여부 (요청 답변일 경우만 사용)

    private boolean isHidden = false; // 숨김 여부 (신고 누적 시 true)

    private int viewCount = 0; // 조회수 (자발적 공유일 경우 사용)

    @Column
    private Double lat;

    @Column
    private Double lng;

    // ─────────────────────────────────────────────
    // 관계 매핑
    // ─────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = true)
    private Request request; // 연결된 요청 (null 가능)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter; // 작성자 (로그 등록자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = true)
    private Place place; // 관련 장소

}
