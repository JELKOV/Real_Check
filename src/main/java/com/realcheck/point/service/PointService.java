package com.realcheck.point.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.point.dto.PointDto;
import com.realcheck.point.entity.Point;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.user.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * PointService
 * - 포인트 지급 및 조회 기능을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;

    // ─────────────────────────────────────────────
    // [1] 포인트 지급 처리
    // ─────────────────────────────────────────────

    /**
     * [1] 포인트 지급 메서드
     * - 특정 사용자에게 포인트를 지급
     * - 지급 사유 및 시점 기록
     *
     * @param user   포인트를 받을 사용자
     * @param amount 지급할 포인트 수
     * @param reason 지급 사유 (예: 상태 등록 보상)
     */
    public void givePoint(User user, int amount, String reason) {
        Point point = new Point();
        point.setUser(user);
        point.setAmount(amount);
        point.setReason(reason);
        point.setEarnedAt(LocalDateTime.now());

        pointRepository.save(point);
    }

    // ─────────────────────────────────────────────
    // [2] 포인트 내역 조회
    // ─────────────────────────────────────────────

    /**
     * [2] 포인트 내역 조회 메서드
     * - 특정 사용자의 포인트 지급 이력을 모두 조회
     *
     * @param userId 사용자 ID
     * @return 포인트 목록 (List<PointDto>)
     */
    public List<PointDto> getPointsByUserId(Long userId) {
        return pointRepository.findByUserId(userId).stream()
                .map(PointDto::fromEntity)
                .toList();
    }

}
