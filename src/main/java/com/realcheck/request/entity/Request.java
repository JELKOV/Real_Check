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

    // 요청 ID (기본키)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요청 등록한 사용자 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 요청 제목 / 설명 / 요청에 걸린 포인트
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer point;

    // 요청 카테고리 (12가지 중 하나)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestCategory category;

    /**
     * 장소 정보 (택 1 구조)
     *
     * 공식 등록된 장소인 경우 → place 객체 연결
     * 자유입력 장소인 경우 → customPlaceName, lat/lng 필드 사용
     * 둘 다 null일 수 없도록, 프론트 또는 컨트롤러에서 유효성 검증 필요
     */
    // 공식 등록 장소일 경우
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    // 사용자 입력 장소 정보 (자유 형식일 때)
    private String customPlaceName;
    private Double lat;
    private Double lng;

    // 요청 마감 여부 (답변 채택 시 true)
    private boolean isClosed;

    // 생성 시각
    private LocalDateTime createdAt;

    /**
     * 연결된 상태 로그 (답변들)
     * - 하나의 요청에 여러 개의 답변(StatusLog)이 연결됨
     * - 양방향 연관 관계
     * - Request → StatusLog 는 mappedBy로 관리됨 (읽기 전용)
     */
    @Builder.Default
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StatusLog> statusLogs = new ArrayList<>();

    // 자동 동기화 로직 (답변 선택 시 자동 마감)
    @PreUpdate
    private void syncClosedWithSelected() {
        if (statusLogs != null && statusLogs.stream().anyMatch(StatusLog::isSelected)) {
            this.isClosed = true;
        }
    }

    // ─────────────────────────────────────────────
    // 카테고리별 유연 필드 (nullable 허용)
    // ─────────────────────────────────────────────

    private Integer waitCount; // WAITING_STATUS, CROWD_LEVEL
    private Boolean hasBathroom; // BATHROOM
    private String menuInfo; // FOOD_MENU
    private String weatherNote; // WEATHER_LOCAL
    private String vendorName; // STREET_VENDOR
    private String photoNote; // PHOTO_REQUEST
    private String noiseNote; // NOISE_LEVEL
    private Boolean isOpen; // BUSINESS_STATUS
    private Integer seatCount; // OPEN_SEAT
    private Boolean isParkingAvailable; // PARKING

    // (선택) 기타 확장 정보 - JSON 형식 등으로 활용 가능
    @Column(columnDefinition = "TEXT")
    private String extra;

}
