package com.realcheck.status.entity;

import java.time.LocalDateTime;

import com.realcheck.place.entity.Place;
import com.realcheck.request.entity.Request;
import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String content; // 상태 설명

    private boolean rewarded = false; // 조회수 기반 보상 지급 여부

    @Column(nullable = true)
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

    // ──────────────── 추가된 유연 필드들 ────────────────

    @Column
    private Boolean hasBathroom; // 화장실 여부

    @Column
    private String menuInfo; // 음식 메뉴

    @Column
    private Integer waitCount; // 대기 인원 수

    @Column
    private String weatherNote; // 날씨 상태

    @Column
    private String vendorName; // 노점 이름

    @Column
    private String photoNote; // 사진 요청 메모

    @Column
    private String noiseNote; // 소음 상태

    @Column
    private Boolean isParkingAvailable; // 주차 가능 여부

    @Column
    private Boolean isOpen; // 영업 여부

    @Column
    private Integer seatCount; // 남은 좌석 수

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

    // 자동 동기화 로직 (답변 채택 시)
    @PreUpdate
    private void syncRequestOnSelection() {
        if (this.isSelected && this.request != null) {
            this.request.setClosed(true);
        }
    }
}
