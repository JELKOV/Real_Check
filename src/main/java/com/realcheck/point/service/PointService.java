package com.realcheck.point.service;

import java.time.LocalDateTime;
import java.util.List;
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
    // [1] 포인트 지급 처리
    // ─────────────────────────────────────────────

    /**
     * AutoCloseRequestService: distributePointsToAnswerers
     * StatusLogService: giveUserPoint / selectAnswer
     * [1] 포인트 지급/차감 처리 (트랜잭션)
     */
    @Transactional
    public void givePoint(User user, int amount, String reason, PointType type) {
        if (amount == 0) {
            throw new IllegalArgumentException("지급할 포인트가 0입니다.");
        }

        if (amount < 0 && user.getPoints() < Math.abs(amount)) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // [1] 포인트 지급 기록
        Point point = Point.builder()
                .user(user)
                .amount(amount)
                .reason(reason)
                .earnedAt(LocalDateTime.now())
                .type(type)
                .build();
        pointRepository.save(point);

        // [2] 사용자 포인트 누적 반영
        user.setPoints(user.getPoints() + amount);
        userRepository.save(user);
    }

    // ─────────────────────────────────────────────
    // [2] 포인트 내역 조회
    // ─────────────────────────────────────────────

    /**
     * [2] 포인트 내역 조회 메서드
     * - 특정 사용자의 포인트 지급 이력을 모두 조회
     */
    @Transactional(readOnly = true)
    public List<PointDto> getPointsByUserId(Long userId) {
        return pointRepository.findByUserId(userId).stream()
                .map(PointDto::fromEntity)
                .toList();
    }

}
