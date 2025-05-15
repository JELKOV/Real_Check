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
    // [1] 기본 필드
    // ─────────────────────────────────────────────

    // 기본 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상태 설명
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String content;

    // 등록 시간 (기본값: 현재)
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 상태 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType statusType = StatusType.ANSWER;

    // 조회수 기반 보상 지급 여부
    @Column(nullable = false)
    private boolean rewarded = false;

    // 이미지 경로 (선택적)
    @Column(nullable = true)
    private String imageUrl;

    // 답변 채택 여부 (요청 답변일 경우만 사용)
    private boolean isSelected = false;

    // 숨김 여부 (신고 누적 시 true)
    @Column(nullable = false)
    private boolean isHidden = false;

    // 조회수 (자발적 공유일 경우 사용)
    @Column(nullable = false)
    private int viewCount = 0;

    // ─────────────────────────────────────────────
    // [2] 유연 필드 (카테고리별 동적 사용)
    // - 카테고리에 따라 동적으로 사용되며, null 허용
    // ─────────────────────────────────────────────

    // 화장실 여부 (BATHROOM)
    @Column
    private Boolean hasBathroom;
    // 메뉴 정보 (FOOD_MENU)
    @Column
    private String menuInfo;
    // 대기 인원 (WAITING_STATUS, CROWD_LEVEL)
    @Column
    private Integer waitCount;
    // 날씨 상태 (WEATHER_LOCAL)
    @Column
    private String weatherNote;
    // 노점 이름 (STREET_VENDOR)
    @Column
    private String vendorName;
    // 사진 요청 메모 (PHOTO_REQUEST)
    @Column
    private String photoNote;
    // 소음 상태 (NOISE_LEVEL)
    @Column
    private String noiseNote;
    // 주차 가능 여부 (PARKING)
    @Column
    private Boolean isParkingAvailable;
    // 영업 여부 (BUSINESS_STATUS)
    @Column
    private Boolean isOpen;
    // 남은 좌석 수 (OPEN_SEAT)
    @Column
    private Integer seatCount;
    // 혼잡도 (CROWD_LEVEL)
    @Column
    private Integer crowdLevel;
    // 기타 정보 (ETC) - 사용자 자유 입력
    @Column(columnDefinition = "TEXT")
    private String extra;

    // ─────────────────────────────────────────────
    // [3] 위치 필드 (좌표)
    // ─────────────────────────────────────────────

    // 위도 (Latitude)
    @Column
    private Double lat;

    // 경도 (Longitude)
    @Column
    private Double lng;

    // ─────────────────────────────────────────────
    // [4] 관계 매핑
    // ─────────────────────────────────────────────
    
    // 연결된 요청 (답변형 StatusLog일 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = true)
    private Request request; 

    // 작성자 (로그 등록자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter; 

    // 관련 장소 (선택적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = true)
    private Place place; 
}
