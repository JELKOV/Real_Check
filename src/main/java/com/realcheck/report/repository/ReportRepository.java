package com.realcheck.report.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.realcheck.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * [1] 특정 StatusLog에 대해 신고된 횟수를 조회
     * ReportAdminService: countReportsByStatusLogId
     * - 신고 수 조회용
     */
    long countByStatusLogId(Long statusLogId);

    /**
     * [2] 특정 사용자가 해당 상태 로그에 신고한 여부 확인
     * ReportService: hasAlreadyReported
     * ReportAdminService: countReportsByStatusLogId
     */
    boolean existsByReporterIdAndStatusLogId(Long userId, Long statusLogId);

    /**
     * [3] 특정 사용자의 신고 기록 객체 조회 (신고 취소용)
     * ReportService: cancelReport
     */
    Report findByReporterIdAndStatusLogId(Long userId, Long statusLogId);

    /**
     * [4] 사용자가 신고한 모든 Report 조회
     * UserAdminService: getUserReports
     * - 특정 사용자가 신고한 모든 Report 객체를 조회
     * - 관리자 화면에서 사용자 활동 로그 조회 시 사용
     */
    Page<Report> findByReporterId(Long userId, Pageable pageable);

}
