package com.realcheck.status.service;

import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.point.service.PointService;
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
    private final PointService pointService;

    /**
     * 대기 현황 등록 메서드
     * - 로그인한 유저가 특정 장소에 대한 실시간 상태 정보를 등록
     * - 1일 3회 등록 제한, 포인트 지급 포함
     *
     * @param userId 로그인한 유저의 ID (세션에서 전달받음)
     * @param dto    상태 로그 등록 정보 (대기 설명, 인원, 이미지, 장소ID)
     */
    public void register(Long userId, StatusLogDto dto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 2. 경고 누적 상태인지 처리
        if (!user.isActive()) {
            throw new RuntimeException("해당 사용자는 신고 누적으로 차단되었습니다.");
        }
        // 3. 장소 조회
        Place place = placeRepository.findById(dto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        // 4. 하루 3회 등록 제한 검사 (자정~자정 기준)
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int count = statusLogRepository.countByReporterIdAndCreatedAtBetween(userId, start, end);
        if (count >= 3) {
            throw new RuntimeException("하루 3회까지만 등록 가능합니다.");
        }

        // 5. StatusLog 엔티티 생성 및 저장
        StatusLog log = dto.toEntity(user, place);
        statusLogRepository.save(log);
        // 6. 포인트 지급 처리
        pointService.givePoint(user, 10, "정보 공유");
    }

    /**
     * 특정 장소에 대한 상태 로그 목록 조회
     * - 장소 ID를 기준으로 최신순 정렬된 로그 목록 반환
     *
     * @param placeId 장소의 고유 ID
     * @return 상태 로그 DTO 리스트
     */
    public List<StatusLogDto> getLogsByPlace(Long placeId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        return statusLogRepository.findRecentByPlaceId(placeId, cutoff)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * 전체 상태 로그(StatusLog)를 조회하는 메서드
     * 
     * 관리자 전용 기능에서 사용됩니다 (/api/status/all)
     * 
     * 동작 설명:
     * - statusLogRepository.findAll() 을 통해 모든 로그(Entity)를 조회
     * - Entity → DTO 변환을 위해 map(StatusLogDto::fromEntity) 사용
     * - 변환된 List<StatusLogDto> 를 반환
     *
     * @return 모든 StatusLog를 DTO로 변환한 리스트
     */

    public List<StatusLogDto> getAllLogs() {
        return statusLogRepository.findAll().stream()
                .map(StatusLogDto::fromEntity) // Entity → DTO 변환
                .toList(); // 변환된 리스트 반환
    }
}