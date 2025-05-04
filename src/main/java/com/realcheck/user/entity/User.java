package com.realcheck.user.entity;

import java.util.ArrayList;
import java.util.List;

import com.realcheck.place.entity.Place;
import com.realcheck.point.entity.Point;
import com.realcheck.report.entity.Report;
import com.realcheck.request.entity.Request;
import com.realcheck.status.entity.StatusLog;

// JPA에서 DB 테이블과 자바 클래스를 매핑할 때 사용하는 어노테이션
import jakarta.persistence.*;
// Lombok은 반복되는 getter/setter, 생성자 등을 자동 생성해주는 도구
import lombok.*;

/**
 * User 엔티티 클래스
 * - 회원 정보를 저장하는 테이블 (users)과 매핑됨
 * - 일반 사용자, 제휴 업체, 관리자 역할을 포함
 */
@Entity // 해당 클래스가 JPA에서 사용할 Entity 클래스임을 명시 (테이블과 매핑됨)
@Table(name = "users") // 실제 DB에 생성될 테이블 이름 지정 (기본은 클래스명)
@Getter // 모든 필드에 대해 getter 메서드 자동 생성
@Setter // 모든 필드에 대해 setter 메서드 자동 생성
@NoArgsConstructor // 파라미터 없는 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 매개변수로 받는 생성자 자동 생성
@ToString // toString() 메서드 자동 생성 (디버깅 용이)
public class User {

    // 기본 키(PK) 설정 - 각 유저를 고유하게 식별
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment와 유사하게 DB가 자동으로 ID 생성
    private Long id;

    // 이메일 주소 - 중복 허용하지 않으며 반드시 입력되어야 함
    @Column(nullable = false, unique = true)
    private String email;

    // 닉네임 - 중복 불가, 필수 입력
    @Column(nullable = false, unique = true)
    private String nickname;

    // 비밀번호 - 필수 입력
    @Column(nullable = false)
    private String password;

    // 사용자 역할 (일반 사용자, 제휴 업체, 관리자) - Enum 타입으로 관리
    @Enumerated(EnumType.STRING)
    private UserRole role;

    // 포인트 - 기본값 0으로 시작, 정보 제공 등으로 증가 가능
    private int points = 0;

    // 활성 상태 여부 - true면 정상 활동, false면 비활성화된 계정
    private boolean isActive = true;

    // ──────────────────────────────
    // 관계 설정 (양방향)
    // ──────────────────────────────

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Request> requests = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StatusLog> statusLogs = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Point> pointLogs = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Place> places = new ArrayList<>();

}
