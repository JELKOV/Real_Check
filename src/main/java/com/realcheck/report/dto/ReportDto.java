package com.realcheck.report.dto;

import java.time.LocalDateTime;

import com.realcheck.report.entity.Report;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {

    // 동시성 제어용 버전 필드 추가
    private Integer version;

    // PK
    private Long id;
    // 신고사유(ex. "정보가 틀려요")
    private String reason;
    // 신고 대상 상태 로그 ID
    private Long statusLogId;
    // 신고 횟수
    private int reportCount;
    // 신고 한 시각
    private LocalDateTime createdAt;

    // 신고자 이메일
    private String reporterEmail;

    // 작성자 이메일 (상태 로그 작성자)
    private String writerEmail;

    // ─────────────────────────────────────────────────
    // 상태 로그(답변) 본문
    // ─────────────────────────────────────────────────
    private String statusLogContent;

    // ─────────────────────────────────────────────────
    // 상태 로그가 달린 요청(질문) 정보
    // ─────────────────────────────────────────────────
    private Long requestId; // 요청 ID (답변형 로그일 때만 있음)
    private String requestTitle; // 요청 제목 (답변형 로그일 때)
    private String requestContent; // 요청 본문 (답변형 로그일 때)

    /**
     * DTO -> Entity 변환
     * - Controller -> Service -> Entity 저장할 때 사용용
     */

    public Report toEntity(User reporter, StatusLog statusLog) {
        Report report = new Report();
        report.setReason(this.reason);
        report.setReporter(reporter);
        report.setStatusLog(statusLog);
        return report;
    }

    /**
     * Entity → DTO 변환
     */
    public static ReportDto fromEntity(Report report) {
        // (1) 신고자 이메일
        String reporterEmail = null;
        if (report.getReporter() != null) {
            reporterEmail = report.getReporter().getEmail();
        }

        // (2) 상태 로그 객체
        StatusLog log = report.getStatusLog();

        // (3) 상태 로그 작성자 이메일
        String writerEmail = null;
        if (log != null && log.getReporter() != null) {
            writerEmail = log.getReporter().getEmail();
        }

        // (4) 상태 로그 본문(content)
        String statusLogContent = (log != null ? log.getContent() : null);

        // (5) 상태 로그가 속한 요청(request) 정보
        Long requestId = null;
        String requestTitle = null;
        String requestContent = null;
        if (log != null && log.getRequest() != null) {
            requestId = log.getRequest().getId();
            requestTitle = log.getRequest().getTitle();
            requestContent = log.getRequest().getContent();
        }

        return ReportDto.builder()
                .version(report.getVersion())
                .id(report.getId())
                .reason(report.getReason())
                .statusLogId(log != null ? log.getId() : null)
                .reportCount(log != null ? log.getReportCount() : 0)
                .createdAt(report.getCreatedAt())
                .reporterEmail(reporterEmail)
                .writerEmail(writerEmail)
                .statusLogContent(statusLogContent)
                .requestId(requestId)
                .requestTitle(requestTitle)
                .requestContent(requestContent)
                .build();
    }

}
