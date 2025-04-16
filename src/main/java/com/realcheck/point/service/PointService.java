package com.realcheck.point.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.point.dto.PointDto;
import com.realcheck.point.entity.Point;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;

    // 포인트 지급 처리
    public void givePoint(User user, int amount, String reason) {
        Point point = new Point();
        point.setUser(user);
        point.setAmount(amount);
        point.setReason(reason);
        point.setEarnedAt(LocalDateTime.now());

        pointRepository.save(point);
    }

    // 포인트 조회
    public List<PointDto> getPointsByUserId(Long userId) {
        return pointRepository.findByUserId(userId).stream()
                .map(PointDto::fromEntity)
                .toList();
    }

}
