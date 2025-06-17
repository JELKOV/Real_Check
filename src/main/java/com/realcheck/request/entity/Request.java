package com.realcheck.request.entity;

import com.realcheck.place.entity.Place;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Request 엔티티 클래스
 *
 * 사용자가 등록한 요청 정보
 * - 제목, 내용, 포인트
 * - 장소 정보 (공식 등록 장소 or 사용자 자유입력)
 * - 요청 시각 및 마감 여부
 * - 연결된 상태 로그(StatusLog)들과의 관계를 포함
 * - 카테고리에 따라 일부 필드는 유동적으로 사용
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    // ─────────────────────────────────────────────
    // [0] 동시성 제어
    // ─────────────────────────────────────────────

    // 동시성 제어를 위한 버전 필드 추가
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (식별자, 제목, 내용, 포인트)
    // ─────────────────────────────────────────────

    // 요청 ID (기본키)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요청 제목
    @Column(nullable = false)
    private String title;

    // 요청 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 요청에 걸린 포인트
    private Integer point;

    // 요청 카테고리 (12가지 중 하나)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestCategory category;

    // 포인트 처리 여부 (환불 or 분배 여부)
    @Builder.Default
    @Column(nullable = false)
    private boolean pointHandled = false;

    // 환불 처리가 된 요청만 이 값이 true
    @Column(nullable = false)
    @Builder.Default
    private boolean refundProcessed = false;

    // ─────────────────────────────────────────────
    // [2] 연관 관계 설정 (사용자, 장소, 상태 로그)
    // ─────────────────────────────────────────────

    // 요청 등록한 사용자 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 공식 등록 장소 (선택적 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    /**
     * 연결된 상태 로그 (답변들)
     * - 하나의 요청에 여러 개의 답변(StatusLog)이 연결됨
     * - 양방향 연관 관계
     * - Request → StatusLog 는 mappedBy로 관리됨 (읽기 전용)
     */
    @Builder.Default
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StatusLog> statusLogs = new ArrayList<>();

    // ─────────────────────────────────────────────
    // [3] 장소 정보 (사용자 지정 장소 지원)
    // ─────────────────────────────────────────────

    // 사용자 입력 장소 정보 (자유 형식일 때)
    private String customPlaceName;

    // 사용자 지정 장소 좌표 (위도, 경도)
    private Double lat;
    private Double lng;

    // ─────────────────────────────────────────────
    // [4] 상태 필드 (생성 시각, 마감 여부)
    // ─────────────────────────────────────────────

    // 요청 마감 여부 (답변 채택 시 true)
    @Builder.Default
    @Column(nullable = false)
    private boolean isClosed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시간 (업데이트 시 자동 설정)
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────
    // [5] 카테고리별 동적 필드 (nullable 허용)
    // ─────────────────────────────────────────────

    // 대기 인원 (WAITING_STATUS, CROWD_LEVEL)
    private Integer waitCount;
    // 혼잡도 (CROWD_LEVEL) - 대기 인원과 통합 사용 가능
    private Integer crowdLevel;
    // 화장실 여부 (BATHROOM)
    private Boolean hasBathroom;
    // 메뉴 정보 (FOOD_MENU)
    private String menuInfo;
    // 날씨 상태 (WEATHER_LOCAL)
    private String weatherNote;
    // 노점 이름 (STREET_VENDOR)
    private String vendorName;
    // 사진 메모 (PHOTO_REQUEST)
    private String photoNote;
    // 소음 상태 (NOISE_LEVEL)
    private String noiseNote;
    // 영업 여부 (BUSINESS_STATUS)
    private Boolean isOpen;
    // 남은 좌석 수 (OPEN_SEAT)
    private Integer seatCount;
    // 주차 가능 여부 (PARKING)
    private Boolean isParkingAvailable;
    // 기타 정보 (ETC) - 사용자 자유 입력
    @Column(columnDefinition = "TEXT")
    private String extra;

}
