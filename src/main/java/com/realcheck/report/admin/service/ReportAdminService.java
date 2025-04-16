package com.realcheck.report.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 신고 서비스
 * - 신고 전체 조회, 통계 등 관리자 기능 처리
 */
@Service
@RequiredArgsConstructor
public class ReportAdminService {

    private final ReportRepository reportRepository;

    /**
     * 전체 신고 내역 조회
     * - Report → ReportDto 변환하여 반환
     * 
     * @return 신고 목록 리스트
     */
    public List<ReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportDto::fromEntity) // 엔티티 → DTO 변환
                .toList();
    }

    /**
     * 특정 상태 로그(StatusLog)에 대한 신고 횟수 조회
     *
     * - statusLogId를 기준으로 해당 로그가 얼마나 신고되었는지 확인
     * - 관리자 페이지에서 신고 누적 판단 또는 통계용으로 사용 가능
     *
     * @param statusLogId 신고 수를 조회할 대상 로그 ID
     * @return 신고 횟수 (long)
     */
    public long countReportsByStatusLogId(Long statusLogId) {
        return reportRepository.countByStatusLogId(statusLogId);
    }
}
