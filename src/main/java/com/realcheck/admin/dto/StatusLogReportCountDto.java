package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * StatusLog별 신고 누적 건수 정보 전달용 DTO
 * - statusLogId : 해당 상태 로그의 ID
 * - count       : 누적된 신고 건수
 */
@Data
@AllArgsConstructor
public class StatusLogReportCountDto {
    private Long statusLogId;
    private Long count;
}
