package com.realcheck.report.dto;

import java.time.LocalDateTime;

import com.realcheck.report.entity.Report;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    // 동시성 제어용 버전 필드 추가
    private Integer version;

    // 신고사유(ex. "정보가 틀려요")
    private String reason;
    // 신고 대상 상태 로그 ID
    private Long statusLogId;
    // 신고 횟수
    private int reportCount;
    // 신고 한 시각
    private LocalDateTime createdAt;

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

    public static ReportDto fromEntity(Report report) {
        return new ReportDto(
                report.getVersion(),
                report.getReason(),
                report.getStatusLog() != null ? report.getStatusLog().getId() : null,
                report.getStatusLog() != null ? report.getStatusLog().getReportCount() : 0,
                report.getCreatedAt());
    }

}
