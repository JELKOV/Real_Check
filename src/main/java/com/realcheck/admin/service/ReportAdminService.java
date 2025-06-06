package com.realcheck.admin.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.TargetType;
import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.entity.Report;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * ReportAdminService (ALL DONE)
 * - 관리자 전용 신고 서비스
 * - 전체 신고 목록 조회, 신고 건수 통계 등 처리
 */
@Service
@RequiredArgsConstructor
public class ReportAdminService {

    private final ReportRepository reportRepository;
    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;
    private final AdminActionLogService adminActionLogService;

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
     * [3] 신고된 상태 로그 (숨김 처리된 로그) 조회
     * ReportAdminController: getReportedLogs
     * - 숨김 처리된 상태 로그만 조회
     * - 관리자 페이지에서 신고된 상태 로그 확인용
     */
    public List<StatusLogDto> getHiddenLogs() {
        return statusLogRepository.findByIsHiddenTrue().stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [4] 특정 StatusLog에 딸린 개별 신고 목록 조회
     * ReportAdminController: getReportsForStatusLog
     * - 특정 상태 로그에 대한 모든 신고 내역을 조회
     * - 관리자 페이지에서 특정 상태 로그에 대한 신고 내역 확인용
     */
    public List<ReportDto> getReportsForStatusLog(Long statusLogId) {
        return reportRepository.findByStatusLogId(statusLogId, Pageable.unpaged()).stream()
                .map(ReportDto::fromEntity)
                .toList();
    }

    /**
     * [5] 관리자가 오탐 신고를 삭제할 때 호출
     * ReportAdminController: deleteReport
     * - 신고 ID와 관리자 ID를 받아 해당 신고를 삭제
     * - 삭제 시 연관된 StatusLog의 reportCount 감소 및 숨김 상태 재계산
     * - 신고 당한 사용자(User)의 reportCount도 감소
     * - 삭제 로그 기록은 선택 사항으로 구현 가능
     */
    @Transactional
    public void deleteReport(Long reportId, Long adminId) {
        // (1) 삭제 대상 Report 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다. id=" + reportId));

        // (2) 연관된 StatusLog 및 User 가져오기
        StatusLog log = report.getStatusLog();
        User reportedUser = null;
        if (log != null) {
            reportedUser = log.getReporter(); // 해당 상태 로그를 작성한 사용자
        }

        // (3) Report 엔티티 삭제
        reportRepository.delete(report);

        // (4) StatusLog.reportCount 감소 및 hidden 상태 재계산
        if (log != null) {
            // reportCount가 0보다 크면 감소
            if (log.getReportCount() > 0) {
                log.decrementReportCount();
            }
            // 만약 reportCount < 3 이면 자동 숨김 해제
            if (log.getReportCount() < 3 && log.isHidden()) {
                log.setHidden(false);
            }
            statusLogRepository.save(log);
        }

        // (5) 신고 당한 사용자(User) reportCount 감소
        if (reportedUser != null && reportedUser.getReportCount() > 0) {
            reportedUser.decrementReportCount();
            userRepository.save(reportedUser);
        }

        // (6) 관리자 행동 로그 기록
        // ActionType.REJECT 을 사용했습니다. “REPORT”를 대상으로 “REJECT(거절/삭제)” 동작을 했다는 의미
        adminActionLogService.saveLog(
                adminId, // 삭제를 수행한 관리자 ID
                reportId, // 삭제된 Report ID
                ActionType.REJECT, // ActionType: 여기서는 REJECT(=거절/삭제)로 사용
                TargetType.REPORT, // TargetType: 삭제 대상이 ‘신고(REPORT)’임을 표시
                "관리자에 의해 오탐 신고(Report ID=" + reportId + ") 삭제됨");

    }

}
