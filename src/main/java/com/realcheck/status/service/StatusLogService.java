package com.realcheck.status.service;

import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * StatusLogService
 * - 대기 현황 등록, 조회 등 핵심 비즈니스 로직을 처리하는 서비스 계층
 */
@Service
@RequiredArgsConstructor
public class StatusLogService {

    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    /**
     * 대기 현황 등록
     * - 로그인 유저 ID를 기반으로 등록자 식별
     * - 하루 최대 3회 등록 제한
     * - Place 존재 확인 및 관계 설정
     */
    public void register(Long userId, StatusLogDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Place place = placeRepository.findById(dto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        // 하루 3회 등록 제한 검사
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int count = statusLogRepository.countByReporterIdAndCreatedAtBetween(userId, start, end);
        if (count >= 3) {
            throw new RuntimeException("하루 3회까지만 등록 가능합니다.");
        }

        // 엔티티로 변환 후 저장
        StatusLog log = dto.toEntity(user, place);
        statusLogRepository.save(log);
        // TODO: 포인트 지급 로직 추가 가능
    }

    /**
     * 특정 장소에 대한 최근 대기 현황 리스트 조회
     */
    public List<StatusLogDto> getLogsByPlace(Long placeId) {
        return statusLogRepository.findByPlaceIdOrderByCreatedAtDesc(placeId)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }
}