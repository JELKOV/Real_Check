package com.realcheck.place.entity;

import com.realcheck.user.entity.User;

// JPA에서 DB 매핑을 위한 어노테이션 제공
import jakarta.persistence.*;
// 코드를 짧게 줄여주는 자동 생성 도구 (생성자, getter/setter)
import lombok.*;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private Double latitude;
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // 제휴 업체 소유자
}
