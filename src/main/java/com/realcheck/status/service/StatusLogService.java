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

    // ────────────────────────────────────────
    // [1] 사용자 기능
    // ────────────────────────────────────────

    /**
     * [1-1] 대기 현황 등록 (사용자용)
     * - 유저 차단 여부 확인
     * - 장소 존재 여부 확인
     * - 하루 등록 횟수 3회 제한
     * - 포인트 지급 포함
     *
     * @param userId 로그인한 사용자 ID
     * @param dto    상태 로그 등록 데이터
     */
    public void register(Long userId, StatusLogDto dto) {
        // 1. 사용자 조회 및 차단 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        if (!user.isActive()) {
            throw new RuntimeException("해당 사용자는 신고 누적으로 차단되었습니다.");
        }
        // 2. 장소 조회
        Place place = placeRepository.findById(dto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        // 3. 하루 3회 등록 제한 확인
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int count = statusLogRepository.countByReporterIdAndCreatedAtBetween(userId, start, end);
        if (count >= 3) {
            throw new RuntimeException("하루 3회까지만 등록 가능합니다.");
        }

        // 4. 등록 처리
        StatusLog log = dto.toEntity(user, place);
        statusLogRepository.save(log);

        // 5. 포인트 지급
        pointService.givePoint(user, 10, "정보 공유");
    }

    /**
     * [1-2] 장소별 대기 현황 조회 (3시간 이내) (사용자용-READ)
     * - 장소 ID를 기준으로 최신순 정렬된 로그 목록 반환
     * 
     * @param placeId 장소 ID
     * @return StatusLogDto 리스트 (최신순)
     */
    public List<StatusLogDto> getLogsByPlace(Long placeId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        return statusLogRepository.findRecentByPlaceId(placeId, cutoff)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [1-3] 사용자 본인의 상태 로그 조회 (사용자용-READ)
     *
     * @param userId 사용자 ID
     * @return 본인이 등록한 StatusLog 리스트
     */
    public List<StatusLogDto> getLogsByUser(Long userId) {
        return statusLogRepository.findByReporterIdOrderByCreatedAtDesc(userId).stream().map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [1-4] 상태 로그 수정 (사용자용-UPDATE)
     * - 로그인한 사용자만 가능
     * - 본인이 작성한 로그만 수정 가능
     *
     * @param logId  수정할 로그 ID
     * @param userId 로그인한 사용자 ID
     * @param dto    수정할 데이터 (내용, 대기 인원, 이미지)
     */
    public void updateStatusLog(Long logId, Long userId, StatusLogDto dto) {
        // 1. 상태 로그 조회
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        // 2. 작성자 권한 확인
        if (!log.getReporter().getId().equals(userId)) {
            throw new RuntimeException("해당 로그를 수정할 권한이 없습니다.");
        }

        // 3. 필드 업데이트
        log.setContent(dto.getContent());
        log.setWaitCount(dto.getWaitCount());
        log.setImageUrl(dto.getImageUrl());

        // 4. 저장
        statusLogRepository.save(log);
    }

    /**
     * [1-5] 상태 로그 삭제 (사용자용-DELETE)
     * - 로그인한 사용자만 가능
     * - 본인이 작성한 로그만 삭제 가능
     *
     * @param logId  삭제할 로그 ID
     * @param userId 로그인한 사용자 ID
     */
    public void deleteStatusLog(Long logId, Long userId) {
        // 1. 상태 로그 조회
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        // 2. 작성자 권한 확인
        if (!log.getReporter().getId().equals(userId)) {
            throw new RuntimeException("해당 로그를 삭제할 권한이 없습니다.");
        }
        
        // 3. 삭제 처리
        statusLogRepository.delete(log);
    }

    // ────────────────────────────────────────
    // [2] 관리자 기능
    // ────────────────────────────────────────

    /**
     * [2-1] 전체 StatusLog 목록 조회 (관리자 전용-READ)
     * 
     * 관리자 전용 기능에서 사용됩니다 (/api/status/all)
     * 
     * 동작 설명:
     * - statusLogRepository.findAll() 을 통해 모든 로그(Entity)를 조회
     * - Entity → DTO 변환을 위해 map(StatusLogDto::fromEntity) 사용
     * - 변환된 List<StatusLogDto> 를 반환
     *
     * @return 전체 로그 리스트 (관리자 전용)
     */

    public List<StatusLogDto> getAllLogs() {
        return statusLogRepository.findAll().stream()
                .map(StatusLogDto::fromEntity) // Entity → DTO 변환
                .toList(); // 변환된 리스트 반환
    }

}