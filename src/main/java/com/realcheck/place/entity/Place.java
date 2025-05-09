package com.realcheck.place.entity;

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

    // 장소가 관리자에게 승인되었는지 여부 (기본은 false)
    // 추후 관리자가 검토 후 true로 변경 가능
    @Builder.Default
    private boolean isApproved = false;

    // 하나의 사용자가 여러 장소를 등록할 수 있으므로 다대일 관계
    // DB에서 외래키로 사용될 컬럼 이름은 "owner_id"
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // 장소 등록자 정보 (User 엔티티)

    // 최근 정보
    @Column(name = "recent_info", columnDefinition = "TEXT")
    private String recentInfo; 

    // 커뮤니티 링크
    @Column(name = "community_link", length = 500)
    private String communityLink; 
}
