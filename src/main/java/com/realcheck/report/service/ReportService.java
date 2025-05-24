package com.realcheck.report.service;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.entity.Report;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * ReportService (ALL DONE)
 * - 사용자 신고 처리 및 신고 누적에 따른 자동 제재 처리
 */
@Service // 비즈니스 로직을 수행하는 서비스 계층
@RequiredArgsConstructor // 생성자 주입을 자동으로 처리
public class ReportService {
    private final ReportRepository reportRepository;
    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;

    /**
     * [1] 신고 처리 로직
     * ReportController: report
     * - 신고자가 다른 사용자의 상태 로그를 신고할 때 호출됨
     * - 신고 내용 저장 후, 신고 대상자의 누적 신고 수를 확인
     * - 일정 횟수(3회) 이상이면 자동으로 계정 비활성화 + 해당 로그 숨김 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void report(Long userId, ReportDto dto) {
        // (1) 신고자 유효성 확인
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // (2) 신고 대상 로그 확인
        StatusLog log = statusLogRepository.findById(dto.getStatusLogId())
                .orElseThrow(() -> new RuntimeException("해당 상태 정보 없음"));

        // (3) 동일 사용자의 중복 신고 방지
        if (hasAlreadyReported(userId, log.getId())) {
            throw new RuntimeException("이미 신고한 상태입니다.");
        }

        // (4) 신고 정보 저장
        Report report = dto.toEntity(reporter, log);
        reportRepository.save(report);

        // (5) 신고 대상 로그 신고 횟수 증가 (동시성 안전)
        log.incrementReportCount();
        statusLogRepository.save(log);

        // (6) 신고된 사용자(User) 신고 횟수 증가 (동시성 안전)
        User targetUser = log.getReporter();
        targetUser.incrementReportCount();
        userRepository.save(targetUser);
    }

    /**
     * [2] 중복 신고 확인 (사용자별)
     * ReportController: report
     * ReportController: checkReported
     */
    public boolean hasAlreadyReported(Long userId, Long statusLogId) {
        return reportRepository.existsByReporterIdAndStatusLogId(userId, statusLogId);
    }

    /**
     * [3] 신고 취소 처리
     * ReportController: cancelReport
     * - 신고 기록 삭제 + 대상 로그 및 사용자 신고 횟수 감소
     * - 본인이 신고한 기록이 없으면 예외 발생
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void cancelReport(Long userId, Long statusLogId) {
        // (1) 신고 대상 로그 확인
        StatusLog log = statusLogRepository.findById(statusLogId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보 없음"));

        // (2) 기존 신고 내역 확인
        Report report = reportRepository.findByReporterIdAndStatusLogId(userId, statusLogId);
        if (report == null) {
            throw new RuntimeException("신고 기록이 없습니다.");
        }

        // (3) 신고 기록 삭제
        reportRepository.delete(report);

        // (4) 로그 신고 횟수 감소
        log.decrementReportCount();
        statusLogRepository.save(log);

        // (5) 신고 대상 사용자 신고 횟수 감소
        User targetUser = log.getReporter();
        targetUser.decrementReportCount();
        userRepository.save(targetUser);
    }
}
