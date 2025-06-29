package com.realcheck.point.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.point.dto.PointDto;
import com.realcheck.point.entity.Point;
import com.realcheck.point.entity.PointType;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * PointService
 * - 포인트 지급 및 조회 기능을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // [1] 포인트 지급 및 차감 처리
    // ─────────────────────────────────────────────

    /**
     * [1-1] 포인트 지급/차감 처리
     * AutoCloseRequestService: distributePointsToAnswerers
     * StatusLogService: giveUserPoint
     * StatusLogService: selectAnswer
     * PointService: chargePoint
     */
    @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public void givePoint(User user, int amount, String reason, PointType type) {
        // (1) 지급할 포인트 유효성 검사
        validatePointAmount(amount, user);

        // (2) 포인트 지급 내역 생성 및 저장
        PointDto pointDto = new PointDto(amount, reason, LocalDateTime.now(), type.name());
        Point point = pointDto.toEntity(user, type);
        pointRepository.save(point);

        // (3) 사용자 포인트 누적 반영
        user.setPoints(user.getPoints() + amount);
        userRepository.save(user);
    }

    /**
     * [1-2] FREE_SHARE 보상 회수 처리
     * StatusLogAdminService: blockLog
     * - 상태 로그가 이미 보상(rewarded = true)된 경우,
     * - 사용자에게 지급된 포인트를 회수(-10)하고 보상 여부를 false로 업데이트함
     */
    @Transactional
    public void refundIfRewarded(StatusLog log) {
        if (log.isRewarded()) {
            givePoint(log.getReporter(), -10, "자발적 공유 보상 회수", PointType.REWARD);
            log.setRewarded(false);
        }
    }

    /**
     * [1-3] FREE_SHARE 보상 재지급 처리
     * StatusLogAdminService: unblockLog
     * - 로그가 보상되지 않았고(viewedCount >= 10), 재지급 조건이 충족되면
     * - 사용자에게 포인트 10점을 다시 지급하고 보상 여부를 true로 설정함
     */
    @Transactional
    public void reissueRewardIfEligible(StatusLog log) {
        if (!log.isRewarded() && log.getViewCount() >= 10) {
            givePoint(log.getReporter(), 10, "자발적 공유 보상 재지급", PointType.REWARD);
            log.setRewarded(true);
        }
    }

    /**
     * [1-4] 포인트 충전 메서드
     * PointController: chargePoint
     * - 테스트 모드
     */
    @Transactional
    public UserDto chargePoint(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        givePoint(user, amount, "테스트 포인트 충전", PointType.CHARGE);

        return UserDto.fromEntity(user);
    }

    /**
     * [1-4] 포인트 캐쉬화
     * PointController: cashOut
     * - 테스트 모드
     */
    @Transactional
    public UserDto cashOutPoint(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        givePoint(user, -amount, "포인트 환전 신청", PointType.CASH);

        return UserDto.fromEntity(user);
    }

    // ─────────────────────────────────────────────
    // [2] 포인트 내역 조회
    // ─────────────────────────────────────────────

    /**
     * [2-1] 포인트 내역 조회 메서드
     * PointController: getMyPointsPaged
     * - 특정 사용자의 포인트 지급 이력을 모두 조회
     * - 정렬 기준은 최신순 (earnedAt DESC)
     * - 응답 형태는 Page<PointDto>로 프론트에서 .content, .totalPages 등 사용 가능
     */
    @Transactional(readOnly = true)
    public Page<PointDto> getPagedPointsByUserId(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "earnedAt"));
        return pointRepository.findByUserId(userId, pageable)
                .map(PointDto::fromEntity); // Page.map() 사용
    }

    // ────────────────────────────────────────
    // [*] 내부 공통 메서드
    // ────────────────────────────────────────

    /**
     * [1] 포인트 지급 금액 유효성 검사
     * PointService: givePoint
     * - 0 포인트 지급 불가
     * - 차감 시 사용자의 포인트가 부족하면 예외 발생
     */
    private void validatePointAmount(int amount, User user) {
        if (amount == 0) {
            throw new IllegalArgumentException("지급할 포인트가 0입니다.");
        }

        if (amount < 0 && user.getPoints() < Math.abs(amount)) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }

}
