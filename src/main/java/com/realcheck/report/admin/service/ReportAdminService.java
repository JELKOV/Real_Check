package com.realcheck.report.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.repository.ReportRepository;

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

    /**
     * ReportAdminController: getAllReports
     * [1] 전체 신고 내역 조회
     * - 모든 신고 데이터를 ReportDto 형태로 반환
     * - 관리자 대시보드에서 전체 신고 내용 확인용
     */
    public List<ReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportDto::fromEntity) // 엔티티 → DTO 변환
                .toList();
    }

    /**
     * ReportAdminController: countReports
     * [2] 특정 상태 로그(StatusLog)에 대한 신고 횟수 조회 
     * - 상태 로그 ID를 기준으로 신고 누적 수 반환
     * - 관리자 페이지에서 신고 통계나 누적 판별 용도로 사용
     */
    public long countReportsByStatusLogId(Long statusLogId) {
        return reportRepository.countByStatusLogId(statusLogId);
    }
}
