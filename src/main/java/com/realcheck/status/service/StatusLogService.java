package com.realcheck.status.service;

import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.point.service.PointService;
import com.realcheck.request.entity.Request;
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
    private final com.realcheck.request.repository.RequestRepository requestRepository;
    private final com.realcheck.request.service.RequestService requestService;

    // ────────────────────────────────────────
    // [1] 사용자 기능
    // ────────────────────────────────────────

    /**
     * [1-1] 대기 상태 등록 (기본 타입: ANSWER)
     * - 장소 기반 실시간 상태(대기 인원 등) 등록
     * - 하루 3회 등록 제한
     * - 포인트 지급: 10pt
     */
    public void register(Long userId, StatusLogDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        if (!user.isActive()) {
            throw new RuntimeException("해당 사용자는 신고 누적으로 차단되었습니다.");
        }

        Place place = placeRepository.findById(dto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int count = statusLogRepository.countByReporterIdAndCreatedAtBetween(userId, start, end);
        if (count >= 3) {
            throw new RuntimeException("하루 3회까지만 등록 가능합니다.");
        }

        StatusLog log = dto.toEntity(user, place);
        statusLogRepository.save(log);
        pointService.givePoint(user, 10, "정보 공유");
    }

    /**
     * [1-2] 특정 장소의 상태 로그 조회
     * - 최근 3시간 이내의 로그만 조회
     */
    public List<StatusLogDto> getLogsByPlace(Long placeId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        return statusLogRepository.findRecentByPlaceId(placeId, cutoff)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [1-3] 로그인 사용자의 전체 상태 로그 조회 (작성자 기준)
     */
    public List<StatusLogDto> getLogsByUser(Long userId) {
        return statusLogRepository.findByReporterIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [1-4] 상태 로그 수정
     * - 작성자 본인만 수정 가능
     */
    public void updateStatusLog(Long logId, Long userId, StatusLogDto dto) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        if (!log.getReporter().getId().equals(userId)) {
            throw new RuntimeException("해당 로그를 수정할 권한이 없습니다.");
        }

        log.setContent(dto.getContent());
        log.setWaitCount(dto.getWaitCount());
        log.setImageUrl(dto.getImageUrl());
        statusLogRepository.save(log);
    }

    /**
     * [1-5] 상태 로그 삭제
     * - 작성자 본인만 삭제 가능
     */
    public void deleteStatusLog(Long logId, Long userId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        if (!log.getReporter().getId().equals(userId)) {
            throw new RuntimeException("해당 로그를 삭제할 권한이 없습니다.");
        }

        statusLogRepository.delete(log);
    }

    /**
     * [1-6] 특정 장소의 가장 최근 공개된 상태 1건 조회
     * - 마커 클릭 시 정보 표시용
     */
    public StatusLogDto getLatestVisibleLogByPlaceId(Long placeId) {
        StatusLog log = statusLogRepository.findTopByPlaceIdAndIsHiddenFalseOrderByCreatedAtDesc(placeId);
        return log != null ? StatusLogDto.fromEntity(log) : null;
    }

    /**
     * [1-7] 자발적 정보 공유 등록 (FREE_SHARE)
     * - 요청 없이 사용자가 직접 공유
     * - 조회수 기반 보상 구조로 활용 가능
     */
    public void registerFreeShare(Long userId, StatusLogDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        if (!user.isActive()) {
            throw new RuntimeException("해당 사용자는 신고 누적으로 차단되었습니다.");
        }

        Place place = placeRepository.findById(dto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        StatusLog log = dto.toEntity(user, place);
        log.setStatusType(StatusType.FREE_SHARE); // 자유 공유로 설정
        log.setRequest(null); // 연결 요청 없음
        statusLogRepository.save(log);

        // 조회수 기반 포인트 지급 예정
    }

    /**
     * [1-8] 자발적 공유(FREE_SHARE) 로그 조회 시 조회수 증가
     * - 조회수 1 증가 + 포인트 누적 조건 기반 처리 가능
     */
    public StatusLogDto viewFreeShare(Long logId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 로그가 존재하지 않습니다."));

        if (log.getStatusType() != StatusType.FREE_SHARE) {
            throw new RuntimeException("자발적 공유가 아닙니다.");
        }

        log.setViewCount(log.getViewCount() + 1);
        statusLogRepository.save(log);
        return StatusLogDto.fromEntity(log);
    }

    /**
     * [1-9] 요청 기반 답변 등록 (StatusType = ANSWER)
     * - 특정 요청(Request)에 연결된 답변 등록 처리
     */
    public void registerAnswer(Long userId, StatusLogDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Request request = requestService.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("요청 없음"));

        Place place = placeRepository.findById(dto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        StatusLog log = dto.toEntity(user, place);
        log.setRequest(request);
        log.setStatusType(StatusType.ANSWER);

        statusLogRepository.save(log);
    }

    /**
     * [1-10] 요청 기반 답변 채택 처리
     * - 요청 작성자가 본인 요청에 연결된 답변을 선택
     * - 상태 로그의 selected = true, 요청 마감 처리
     */
    public void selectAnswer(Long statusLogId, Long loginUserId) {
        StatusLog log = statusLogRepository.findById(statusLogId)
                .orElseThrow(() -> new RuntimeException("답변 없음"));
        Request request = log.getRequest();

        if (request == null || !request.getUser().getId().equals(loginUserId)) {
            throw new RuntimeException("채택 권한이 없습니다.");
        }

        log.setSelected(true);
        request.setClosed(true);
        statusLogRepository.save(log);
        requestRepository.save(request);
    }

    // ────────────────────────────────────────
    // [2] 관리자 기능
    // ────────────────────────────────────────

    /**
     * [2-1] 전체 상태 로그 조회 (관리자용)
     * - 모든 로그를 반환
     */
    public List<StatusLogDto> getAllLogs() {
        return statusLogRepository.findAll().stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

}