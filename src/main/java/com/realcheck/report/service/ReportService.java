package com.realcheck.report.service;

import org.springframework.stereotype.Service;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.entity.Report;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * ReportService
 * - 사용자 신고 처리 및 신고 누적에 따른 자동 제재 처리
 */
@Service // 비즈니스 로직을 수행하는 서비스 계층
@RequiredArgsConstructor // 생성자 주입을 자동으로 처리
public class ReportService {
    private final ReportRepository reportRepository;
    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // [1] 사용자 신고 처리
    // ─────────────────────────────────────────────

    /**
     * [1-1] 신고 처리 메서드
     * - 신고자가 다른 사용자의 상태 로그를 신고할 때 호출됨
     * - 신고 내용 저장 후, 신고 대상자의 누적 신고 수를 확인
     * - 일정 횟수(3회) 이상이면 자동으로 계정 비활성화 + 해당 로그 숨김 처리
     *
     * @param userId 신고자 ID (세션에서 가져온 로그인 사용자 ID)
     * @param dto    신고 내용 (신고 사유 + 신고할 StatusLog ID)
     */
    public void report(Long userId, ReportDto dto) {
        // 1. 신고자 유효성 확인
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        // 2. 어떤 상태 로그에 대한 신고인지 확인
        StatusLog log = statusLogRepository.findById(dto.getStatusLogId())
                .orElseThrow(() -> new RuntimeException("해당 상태 정보 없음"));

        // 3. 신고 정보 저장
        Report report = dto.toEntity(reporter, log);
        reportRepository.save(report);

        // 4. 해당 로그를 등록한 유저의 신고 횟수 조회
        Long targetUserId = log.getReporter().getId();
        long reportCount = reportRepository.countByStatusLogReporterId(targetUserId);

        // 5. 신고가 3번 이상이면 사용자 차단
        if (reportCount >= 3) {
            User targetUser = log.getReporter();
            targetUser.setActive(false); // 계정 비활성화
            userRepository.save(targetUser); // 변경된 상태 저장

            log.setHidden(true); // 상태 로그도 숨김 처리
            statusLogRepository.save(log); // 변경사항 저장
        }
    }
}
