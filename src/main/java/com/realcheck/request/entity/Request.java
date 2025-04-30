package com.realcheck.request.entity;

import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Request 엔티티 클래스
 * - 사용자가 등록하는 요청 정보를 담는 클래스
 * - 요청에 대한 답변(StatusLog)이 연결될 수 있음
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키 (자동 생성)

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 요청을 등록한 사용자

    private String title; // 요청 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 요청 상세 설명

    private Integer point; // 요청에 걸린 포인트

    private String placeId; // 장소 식별자 (예: 외부 API 기반)

    private Double lat; // 장소 위도
    private Double lng; // 장소 경도

    private boolean isClosed; // 요청 마감 여부 (답변 채택 시 true)

    private LocalDateTime createdAt; // 요청 등록 시각

    /**
     * 연결된 상태 로그 목록
     * - 하나의 요청에 여러 개의 상태 로그가 연결될 수 있음
     * - 양방향 관계 설정: StatusLog의 request 필드와 매핑됨
     * - @Builder 사용 시 초기값이 유지되도록 @Builder.Default 사용
     */
    @Builder.Default
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StatusLog> statusLogs = new ArrayList<>();
}
