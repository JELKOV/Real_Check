package com.realcheck.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.realcheck.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * 특정 StatusLog에 대해 신고된 횟수를 조회
     *
     * @param statusLogId 신고 대상 상태 로그 ID
     * @return 해당 로그에 대한 신고 개수
     */
    long countByStatusLogId(Long statusLogId); // 신고 수 조회용
}
