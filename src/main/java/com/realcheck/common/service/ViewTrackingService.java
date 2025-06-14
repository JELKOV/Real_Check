package com.realcheck.common.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * ViewTrackingService
 *
 * - 사용자의 조회 기록을 Redis에 저장하여 중복 조회를 방지하는 역할을 수행
 * - 특정 사용자가 특정 상태 로그를 일정 시간 내에 반복 조회하지 못하도록 제한
 * - Redis를 활용해 TTL(Time-To-Live)을 지정하여 자동 만료 처리
 */
@Service
@RequiredArgsConstructor
public class ViewTrackingService {

    // RedisTemplate을 이용하여 String 기반의 키-값 저장소에 접근
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 사용자가 특정 상태 로그를 조회할 수 있는지 여부를 판단
     *
     * @param userId 사용자 ID
     * @param logId  상태 로그 ID
     * @return true: 조회 가능 (중복 아님), false: 최근에 이미 조회한 적 있음
     */
    public boolean canIncreaseView(Long userId, Long logId) {
        // Redis에 저장될 키 형식: viewed:사용자ID:로그ID
        String key = "viewed:" + userId + ":" + logId;

        // 해당 키가 이미 존재하면 최근에 조회한 것이므로 조회수 증가 불가
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists))
            return false;

        // 조회 기록이 없을 경우: Redis에 키를 등록하고 TTL 3시간 설정
        redisTemplate.opsForValue().set(key, "1", Duration.ofHours(3));

        return true;
    }
}
