package com.realcheck.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.realcheck.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * ReportAdminService: countReportsByStatusLogId
     * [1] 특정 StatusLog에 대해 신고된 횟수를 조회
     * - 신고 수 조회용
     * - SQL:
     * SELECT COUNT(*) FROM reports WHERE status_log_id = ?;
     */
    long countByStatusLogId(Long statusLogId);

    /**
     * 
     * [2] 특정 ID의 신고당한 횟수 조회
     * - 신고 당한 횟수
     * - SQL:
     * SELECT COUNT(*) FROM reports r
     * JOIN status_log s ON r.status_log_id = s.id
     * WHERE s.reporter_id = ?;
     */
    long countByStatusLogReporterId(Long userId);
}
