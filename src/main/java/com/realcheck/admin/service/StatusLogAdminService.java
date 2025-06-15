package com.realcheck.admin.service;

import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.TargetType;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.point.service.PointService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


/**
 * StatusLogAdminService
 * - 관리자 전용 FREE_SHARE 로그 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class StatusLogAdminService {

    private final StatusLogRepository statusLogRepository;
    private final AdminActionLogService adminActionLogService;
    private final PointService pointService;

    /**
     * [1] 자발 공유 로그 전체 조회 (관리자 전용)
     * StatusLogAdminController: getFreeShareLogs
     */
    public Page<StatusLogDto> getFreeShareLogs(Pageable pageable) {
        return statusLogRepository.findByStatusType(StatusType.FREE_SHARE, pageable)
                .map(StatusLogDto::fromEntity);
    }

    /**
     * [2] FREE_SHARE 로그 차단 + 포인트 회수 + 로그 기록
     * StatusLogAdminController: blockLog
     */
    public void blockLog(Long logId, Long adminId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("StatusLog not found"));

        if (!log.isHidden()) {
            log.setHidden(true);
            pointService.refundIfRewarded(log); // 포인트 회수
            statusLogRepository.save(log);

            adminActionLogService.saveLog(
                    adminId, log.getId(), ActionType.BLOCK, TargetType.STATUS_LOG, "자발적 공유 로그 차단");
        }
    }

    /**
     * [3] FREE_SHARE 로그 차단 해제 + 로그 기록
     * StatusLogAdminController: unblockLog
     */
    public void unblockLog(Long logId, Long adminId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("StatusLog not found"));

        if (log.isHidden()) {
            log.setHidden(false);
            pointService.reissueRewardIfEligible(log); // 포인트 재지급 (조건 만족 시)
            statusLogRepository.save(log);

            adminActionLogService.saveLog(
                    adminId, log.getId(), ActionType.UNBLOCK, TargetType.STATUS_LOG, "자발적 공유 로그 차단 해제");
        }
    }
}
