package com.realcheck.place.entity;

import com.realcheck.request.entity.RequestCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * AllowedRequestType 엔티티
 * - 특정 장소(Place)에서 허용된 요청 타입을 저장하는 엔티티
 * - 예: 스타벅스에서는 WAITING_STATUS, BUSINESS_STATUS 등만 허용
 */
@Entity
@Table(name = "allowed_request_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllowedRequestType {

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (ID, 타입)
    // ─────────────────────────────────────────────

    // 기본 키 (자동 생성)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 허용된 요청 타입 (Enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestCategory requestType;

    // ─────────────────────────────────────────────
    // [2] 연관 관계 (Place)
    // ─────────────────────────────────────────────

    // 허용된 요청 타입이 연결된 장소 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // ─────────────────────────────────────────────
    // [3] 생성자 (장소 + 요청 타입)
    // ─────────────────────────────────────────────

    /**
     * 생성자 (Place + RequestCategory)
     * 새로운 AllowedRequestType 객체 생성 시 사용
     * 해당 장소에서 허용된 요청 타입을 지정
     */
    public AllowedRequestType(Place place, RequestCategory requestType) {
        this.place = place;
        this.requestType = requestType;
    }

    // ─────────────────────────────────────────────
    // [4] equals & hashCode 오버라이드 (중복 방지)
    // ─────────────────────────────────────────────

    /**
     * equals 메서드
     * requestType과 place ID가 동일하면 동일 객체로 간주
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AllowedRequestType that = (AllowedRequestType) o;
        return requestType == that.requestType &&
                Objects.equals(place != null ? place.getId() : null,
                        that.place != null ? that.place.getId() : null);
    }

    /**
     * hashCode 메서드
     * requestType과 place ID를 기준으로 해시 생성
     */
    @Override
    public int hashCode() {
        return Objects.hash(requestType, place != null ? place.getId() : null);
    }
}
