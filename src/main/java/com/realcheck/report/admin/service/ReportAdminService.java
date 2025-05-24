package com.realcheck.report.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.repository.StatusLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * ReportAdminService
 * - 관리자 전용 신고 서비스
 * - 전체 신고 목록 조회, 신고 건수 통계 등 처리
 */
@Service
@RequiredArgsConstructor
public class ReportAdminService {

    private final ReportRepository reportRepository;
    private final StatusLogRepository statusLogRepository;

    /**
     * [1] 전체 신고 내역 조회
     * ReportAdminController: getAllReports
     * - 모든 신고 데이터를 ReportDto 형태로 반환
     * - 관리자 대시보드에서 전체 신고 내용 확인용
     */
    public List<ReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportDto::fromEntity) // 엔티티 → DTO 변환
                .toList();
    }

    /**
     * [2] 특정 상태 로그(StatusLog)에 대한 신고 횟수 조회
     * ReportAdminController: countReports
     * - 상태 로그 ID를 기준으로 신고 누적 수 반환
     * - 관리자 페이지에서 신고 통계나 누적 판별 용도로 사용
     */
    public long countReportsByStatusLogId(Long statusLogId) {
        return reportRepository.countByStatusLogId(statusLogId);
    }

    /**
     * [3] 신고된 상태 로그 (숨김 처리된 로그) 조회 [미사용]
     * ReportAdminController: getReportedLogs
     * - 숨김 처리된 상태 로그만 조회
     * - 관리자 페이지에서 신고된 상태 로그 확인용
     */
    public List<StatusLogDto> getHiddenLogs() {
        return statusLogRepository.findByIsHiddenTrue().stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }
}
