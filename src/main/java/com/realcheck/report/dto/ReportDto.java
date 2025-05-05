package com.realcheck.report.dto;

import com.realcheck.report.entity.Report;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private String reason;   // 신고사유(ex. "정보가 틀려요")
    private Long statusLogId;// 신고 대상 상태 로그 ID

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
            report.getReason(),
            report.getStatusLog().getId()
        );
    }
    
}
