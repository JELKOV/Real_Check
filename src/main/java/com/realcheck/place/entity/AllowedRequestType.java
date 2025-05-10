package com.realcheck.place.entity;

import com.realcheck.request.entity.RequestCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * AllowedRequestType 엔티티
 * - 각 Place에서 허용된 요청 타입을 저장
 */
@Entity
@Table(name = "allowed_request_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllowedRequestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestCategory requestType;

    /**
     * 생성자 - 허용된 요청 타입 설정
     */
    public AllowedRequestType(Place place, RequestCategory requestType) {
        this.place = place;
        this.requestType = requestType;
    }

    /**
     * equals & hashCode 오버라이드
     * - requestType과 place가 동일하면 동일 객체로 간주
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

    @Override
    public int hashCode() {
        return Objects.hash(requestType, place != null ? place.getId() : null);
    }
}
