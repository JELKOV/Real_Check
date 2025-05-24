package com.realcheck.point.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.point.dto.PointDto;
import com.realcheck.point.entity.Point;
import com.realcheck.point.entity.PointType;
import com.realcheck.point.repository.PointRepository;
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

    // ─────────────────────────────────────────────
    // [2] 포인트 내역 조회
    // ─────────────────────────────────────────────

    /**
     * [2-1] 포인트 내역 조회 메서드 [미사용]
     * - 특정 사용자의 포인트 지급 이력을 모두 조회
     */
    @Transactional(readOnly = true)
    public List<PointDto> getPointsByUserId(Long userId) {
        return pointRepository.findByUserId(userId).stream()
                .map(PointDto::fromEntity)
                .toList();
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
