package com.realcheck.report.entity;

import java.time.LocalDateTime;

import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * 허위 정보에 대한 신고 기록을 저장하는 엔티티
 * - 사용자가 부정확한 정보를 신고하면 이 테이블에 기록됨
 */
@Entity
@Table(name = "reports") // 테이블명: reports
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고 사유 (예: "정보가 틀림", "이미 영업 종료 상태")
    private String reason;

    // 신고 시간 – 기본값은 현재 시간
    private LocalDateTime reportedAt = LocalDateTime.now();

    // 신고한 사용자
    @ManyToOne
    @JoinColumn(name = "reporter_id") // FK 이름은 reporter_id
    private User reporter; // 신고한 사람람

    // 어떤 상태 정보에 대해 신고한 것인지 (StatusLog와 연결)
    @ManyToOne
    @JoinColumn(name = "status_log_id") // FK 이름은 status_log_id
    private StatusLog statusLog; // 신고 대상 로그그
}
