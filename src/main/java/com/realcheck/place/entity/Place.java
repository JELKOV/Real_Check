package com.realcheck.place.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Place 엔티티 클래스
 * - 실제 DB의 "places" 테이블과 매핑되는 클래스
 * - 장소 등록, 조회 등을 위해 사용됨
 */

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    // ─────────────────────────────────────────────
    // [0] 동시성 제어
    // ─────────────────────────────────────────────

    // 동시성 제어를 위한 버전 필드 추가
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (ID, 이름, 주소, 좌표)
    // ─────────────────────────────────────────────

    @Id // 기본 키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 장소 이름 (예: "강남미용실", "스타벅스 역삼점")
    @Column(nullable = false)
    private String name;

    // 도로명 주소 (예: "서울 강남구 테헤란로 123")
    @Column(nullable = false)
    private String address;

    // 위도와 경도
    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    // createdAt: DB에서 자동 생성 (CURRENT_TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
    // [2] 승인 상태 및 등록자 정보
    // ─────────────────────────────────────────────

    // 장소가 관리자에게 승인되었는지 여부 (기본은 false)
    @Builder.Default
    private boolean isApproved = false;

    // 장소 등록자 (User 엔티티와 N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner; // 장소 등록자 정보 (User 엔티티)

    // ─────────────────────────────────────────────
    // [3] 장소 추가 정보 (최근 정보, 커뮤니티 링크)
    // ─────────────────────────────────────────────

    // 최근 장소 정보 (자유 입력, 선택적)
    @Column(name = "recent_info", columnDefinition = "TEXT")
    private String recentInfo;

    // 커뮤니티 링크 (예: 블로그, SNS 링크, 선택적)
    @Column(name = "community_link", length = 500)
    private String communityLink;

    // ─────────────────────────────────────────────
    // [4] 허용된 요청 타입 (AllowedRequestType)
    // ─────────────────────────────────────────────

    /**
     * 허용된 요청 타입 목록 (공식 장소에서만 사용)
     * Set으로 중복 방지
     * CascadeType.ALL: 장소 삭제 시 관련 타입도 함께 삭제
     * orphanRemoval: 외래 키 없는 고아 객체 삭제
     */
    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<AllowedRequestType> allowedRequestTypes = new HashSet<>();
}
