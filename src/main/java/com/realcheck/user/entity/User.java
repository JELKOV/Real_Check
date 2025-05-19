package com.realcheck.user.entity;

import java.time.LocalDateTime;
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
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    // ─────────────────────────────────────────────
    // [1] 기본 사용자 정보 필드
    // ─────────────────────────────────────────────

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

    // 신고 횟수
    @Column(nullable = false)
    private int reportCount = 0;

    // 신고 횟수 증가
    public void incrementReportCount() {
        this.reportCount++;
    }

    // 활성 상태 여부 - true면 정상 활동, false면 비활성화된 계정
    private boolean isActive = true;

    // 회원 생성 시 생성 시점 기록 (createdAt)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 회원 정보 수정 시점 기록 (updatedAt)
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // 마지막 로그인 시점 (lastLogin)
    @Column
    private LocalDateTime lastLogin;

    // 자동 생성 및 갱신 로직
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정 시점만 갱신
    }

    // 탈퇴 예약 여부
    private boolean isPendingDeletion;
    // 탈퇴 예정일
    private LocalDateTime deletionScheduledAt;

    // ─────────────────────────────────────────────
    // [2] 사용자와 연관된 엔티티들 (양방향 관계)
    // ─────────────────────────────────────────────

    /**
     * [2-1] 사용자가 등록한 요청 목록 (Request)
     * User (1) → (N) Request
     * 사용자 탈퇴 시 관련 요청들도 자동 삭제 (CascadeType.ALL)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Request> requests = new ArrayList<>();

    /**
     * [2-2] 사용자가 작성한 상태 로그 (StatusLog)
     * User (1) → (N) StatusLog
     * 사용자 탈퇴 시 관련 로그들도 자동 삭제 (CascadeType.ALL)
     */
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StatusLog> statusLogs = new ArrayList<>();

    /**
     * [2-3] 사용자가 작성한 신고 (Report)
     * User (1) → (N) Report
     * 사용자 탈퇴 시 관련 신고들도 자동 삭제 (CascadeType.ALL)
     */
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();

    /**
     * [2-4] 사용자 포인트 내역 (Point)
     * User (1) → (N) Point
     * 사용자 탈퇴 시 관련 포인트 기록들도 자동 삭제 (CascadeType.ALL)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Point> pointLogs = new ArrayList<>();

    /**
     * [2-5] 사용자가 소유한 장소 (Place)
     * User (1) → (N) Place
     * 사용자 탈퇴 시 관련 장소들도 자동 삭제 (CascadeType.ALL)
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Place> places = new ArrayList<>();

}
