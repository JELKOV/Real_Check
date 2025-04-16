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
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 기본 키

    private String reason; // 신고 사유 (예: "정보가 틀림", "이미 영업 종료 상태")

    private LocalDateTime reportedAt = LocalDateTime.now(); // 신고 시간 – 기본값은 현재 시간

    @ManyToOne
    @JoinColumn(name = "reporter_id") // 신고자와 연결되는 외래키
    private User reporter; // 신고한 사람

    @ManyToOne
    @JoinColumn(name = "status_log_id") // 신고 대상 StatusLog와의 관계
    private StatusLog statusLog; // 어떤 상태 정보를 신고했는지
}
