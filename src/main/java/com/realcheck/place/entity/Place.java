package com.realcheck.place.entity;

import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * Place 엔티티 클래스
 * - 실제 DB의 "places" 테이블과 매핑되는 클래스
 * - 장소 등록, 조회 등을 위해 사용됨
 */

@Entity // 이 클래스는 JPA가 관리하는 엔티티임을 명시
@Table(name = "places") // 이 클래스는 "places"라는 이름의 테이블과 연결됨
@Getter // 모든 필드의 Getter 메서드 자동 생성
@Setter // 모든 필드의 Setter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자 생성
@AllArgsConstructor // 모든 필드를 포함한 생성자 생성
public class Place {

    @Id // 기본 키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // MySQL의 auto_increment 방식으로 ID 자동 생성
    private Long id;

    @Column(nullable = false) // not null 제약 조건
    private String name; // 장소 이름 (예: "강남미용실", "스타벅스 역삼점")

    @Column(nullable = false)
    private String address; // 도로명 주소 (예: "서울 강남구 테헤란로 123")

    // 위도와 경도는 선택적으로 설정 (필수가 아님)
    private double latitude; // 장소의 위도 (예: 37.4979)
    private double longitude; // 장소의 경도 (예: 127.0276)

    // 장소가 관리자에게 승인되었는지 여부 (기본은 false)
    // 추후 관리자가 검토 후 true로 변경 가능
    private boolean isApproved = false;

    // 하나의 사용자가 여러 장소를 등록할 수 있으므로 다대일 관계
    @ManyToOne
    // DB에서 외래키로 사용될 컬럼 이름은 "owner_id"
    @JoinColumn(name = "owner_id")
    private User owner; // 장소 등록자 정보 (User 엔티티)
}
