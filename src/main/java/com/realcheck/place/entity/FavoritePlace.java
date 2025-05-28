package com.realcheck.place.entity;

import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * FavoritePlace
 * - 사용자(User)가 즐겨찾기한 공식 장소(Place)를 나타내는 엔티티
 * - 복합 유니크 제약 조건: (user_id, place_id)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "favorite_place",
    uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "place_id" })
)
public class FavoritePlace {

    /** 즐겨찾기 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 즐겨찾기한 사용자
     * - 여러 즐겨찾기가 한 사용자에게 속할 수 있으므로 @ManyToOne
     * - 지연 로딩(fetch = LAZY)으로 불필요한 조회 방지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 즐겨찾기된 장소
     * - 여러 즐겨찾기가 하나의 장소에 속할 수 있으므로 @ManyToOne
     * - 지연 로딩(fetch = LAZY) 적용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
}
