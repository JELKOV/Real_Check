package com.realcheck.status.service;

import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.common.dto.PageResult;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.point.entity.PointType;
import com.realcheck.point.service.PointService;
import com.realcheck.request.entity.Request;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * StatusLogService
 * - 상태 등록, 수정, 조회 등 핵심 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
public class StatusLogService {

    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PointService pointService;
    private final com.realcheck.request.repository.RequestRepository requestRepository;

    // ─────────────────────────────────────────────
    // [1] 상태 로그 등록 (내부 로직) - CREATE
    // ─────────────────────────────────────────────

    /**
     * StatusLogService: register
     * StatusLogService: registerAnswer
     * [1-1] 상태 로그 등록 내부 메서드 (공통 처리)
     * 사용자 유효성 검사 (userId 존재 여부, 정지 여부 등)
     * 장소 ID가 있는 경우 → 해당 장소 엔티티 조회
     * 상태 로그 생성 (StatusLog.toEntity(user, place))
     * 요청이 필요한 경우 → requestId로 요청 엔티티 연결
     * 상태 로그 저장
     */
    private void registerInternal(Long userId, StatusLogDto dto, StatusType type) {

        // (1) 사용자 유효성 검사 (공통)
        User user = validateUser(userId);
        // (2) 장소 유효성 검사 (Optional)
        Place place = validatePlace(dto.getPlaceId());
        // (3) 공식 장소의 경우 허용된 요청 타입 확인 - 사용자 지정 장소이면 전체 타입 허용
        Request request = validateAndFilterFieldsByRequest(dto);
        if (place != null) {
            validateAllowedRequestType(place, request);
        }

        // 상태 로그 생성 및 저장
        StatusLog log = dto.toEntity(user, place);
        log.setStatusType(type);
        log.setRequest(request);
        statusLogRepository.save(log);
    }

    /**
     * [1-1-A] 사용자 검증 (활성 사용자) - 공통 유효성 검사
     */
    private User validateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        if (!user.isActive()) {
            throw new RuntimeException("해당 사용자는 신고 누적으로 차단되었습니다.");
        }
        return user;
    }

    /**
     * [1-1-B] 장소 검증 (Optional) - 공통 유효성 검사
     */
    private Place validatePlace(Long placeId) {
        if (placeId == null)
            return null;
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소 없음"));
    }

    /**
     * [1-1-C] 요청 검증 및 필드 자동 필터링 - 공통 유효성 검사
     */
    private Request validateAndFilterFieldsByRequest(StatusLogDto dto) {
        if (dto.getRequestId() == null)
            return null;

        Request request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("요청 없음"));

        // 필드 자동 필터링 (StatusLog 엔티티에서 처리)
        dto.filterFieldsByCategory(request.getCategory());
        return request;
    }

    /**
     * [1-1-D] 허용된 요청 타입 검증 (공식 장소)
     */
    private void validateAllowedRequestType(Place place, Request request) {
        if (request == null)
            return; // 요청 없이 자발적 공유일 경우 패스
        boolean isAllowed = place.getAllowedRequestTypes().stream()
                .anyMatch(type -> type.getRequestType().equals(request.getCategory()));
        if (!isAllowed) {
            throw new RuntimeException("해당 장소에서는 선택한 요청 카테고리를 사용할 수 없습니다.");
        }
    }

    // ─────────────────────────────────────────────
    // [2] 사용자 기능 (상태 로그 등록) CREATE
    // ─────────────────────────────────────────────

    /**
     * AnswerController: createAnswer
     * [2-1] 요청 기반 답변 등록
     * 누군가 등록한 요청(Request)에 대해 다른 사용자가 답변(상태 정보)을 등록할 때 사용
     * 장소 정보는 optional (placeId가 null일 수 있음 – custom location 허용)
     */
    @Transactional
    public void registerAnswer(Long userId, StatusLogDto dto, Long requestId) {

        // RequestId가 없는 경우 예외 발생
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID가 전달되지 않았습니다.");
        }
        // (1) 요청 유효성 검사 (존재 확인, 마감 확인)
        Request request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        // (2) 일일 등록 횟수 제한 확인 (3회)
        validateDailyLimit(userId);

        // (3) 마감된 요청인지 확인
        if (request.isClosed()) {
            throw new IllegalStateException("이미 마감된 요청입니다.");
        }

        // (4) 최대 답변 수 확인 (3개)
        if (statusLogRepository.countByRequestIdAndIsHiddenFalse(requestId) >= 3) {
            throw new IllegalStateException("이미 최대 답변 수(3개)에 도달했습니다.");
        }

        // (5) 사용자 중복 답변 확인
        if (hasUserAnswered(requestId, userId)) {
            throw new IllegalArgumentException("이미 답변을 등록하셨습니다.");
        }

        // (6) 공통 등록 로직으로 전달 (유효성 검사 + 상태 로그 생성)
        dto.setRequestId(requestId);

        registerInternal(userId, dto, StatusType.ANSWER);
    }

    /**
     * [2-1-A] 일일 등록 횟수 제한 확인
     */
    private void validateDailyLimit(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int count = statusLogRepository.countByReporterIdAndCreatedAtBetween(userId, start, end);
        if (count >= 10) {
            throw new RuntimeException("하루 10회까지만 등록 가능합니다.");
        }
    }

    /**
     * [2-1-B] 사용자가 동일 요청에 이미 답변을 등록했는지 확인
     */
    private boolean hasUserAnswered(Long requestId, Long userId) {
        return statusLogRepository.existsByRequestIdAndUserId(requestId, userId);
    }

    /**
     * StatusLogController: register [미사용]
     * [2-2] 장소 기반 정보 공유 (공식 장소)
     * 요청 없이 해당 장소 관리자가 직접 공유
     * 공식 등록된 장소(placeId 있음)
     */
    @Transactional
    public void register(Long userId, StatusLogDto dto) {

        // (1) 장소 소유자 확인
        validatePlaceOwnership(userId, dto.getPlaceId());
        // (2) 상태 로그 등록 (공통 로직 호출)
        registerInternal(userId, dto, StatusType.REGISTER);
    }

    /**
     * [2-2-A] 장소 소유자 확인 메서드
     */
    private void validatePlaceOwnership(Long userId, Long placeId) {
        if (placeId == null) {
            throw new IllegalArgumentException("장소 ID가 필요합니다.");
        }

        // 장소 조회 (존재 여부 확인)
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));

        // 사용자 ID와 장소 소유자 ID 비교
        if (!place.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("해당 장소의 소유자가 아닙니다.");
        }
    }

    /**
     * StatusLogController: registerFreeShare [미사용]
     * [2-3] 자발적 정보 공유 등록 (FREE_SHARE)
     * 요청 없이 사용자가 직접 공유
     * 조회수 기반 보상 구조로 활용 가능
     */
    @Transactional
    public void registerFreeShare(Long userId, StatusLogDto dto) {
        registerInternal(userId, dto, StatusType.FREE_SHARE);
    }

    /**
     * [2-4] 자발적 공유(FREE_SHARE): 조회수 증가 및 포인트 지급 [미사용]
     * 조회수 1 증가 + 포인트 누적 조건 기반 처리 가능
     */
    public StatusLogDto viewFreeShare(Long logId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 로그가 존재하지 않습니다."));

        if (log.getStatusType() != StatusType.FREE_SHARE) {
            throw new RuntimeException("자발적 공유가 아닙니다.");
        }

        // 조회수 증가
        log.setViewCount(log.getViewCount() + 1);
        statusLogRepository.save(log);

        // 조회수 기반 포인트 지급 (10회 이상)
        if (log.getViewCount() >= 10 && !log.isRewarded()) {
            giveUserPoint(log.getReporter(), 10, "자발적 정보 조회수 보상");
            log.setRewarded(true); // 중복 지급 방지
            statusLogRepository.save(log);
        }

        return StatusLogDto.fromEntity(log);
    }

    /**
     * [2-4-A] 포인트 지급 처리 (FREE_SHARE에만 사용) [미사용]
     */
    private void giveUserPoint(User user, int points, String reason) {
        pointService.givePoint(user, points, reason, PointType.EARN);
    }

    // ─────────────────────────────────────────────
    // [3] 사용자 기능 (상태 로그 조회) READ
    // ─────────────────────────────────────────────

    /**
     * StatusLogController: getNearbyStatusLogs
     * [3-1] 현재 위치 기반 근처 상태 로그 조회
     * 사용자의 위도/경도를 기준으로 일정 반경 내에 있는 상태 로그를 조회
     * 최근 3시간 이내에 등록된 로그만 반환
     */
    public List<StatusLogDto> findNearbyStatusLogs(double lat, double lng, double radiusMeters) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        return statusLogRepository.findNearbyLogs(lat, lng, radiusMeters, cutoff)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * StatusLogController: getAnswersByRequest
     * [3-2] 특정 요청에 대한 답변 리스트(상세 조회)
     */
    @Transactional(readOnly = true)
    public List<StatusLogDto> getAnswersByRequestId(Long requestId) {
        return statusLogRepository.findByRequestId(requestId).stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * StatusLogController: getMyStatusLogs
     * [3-3] 사용자별 전체 상태 로그 목록 조회 (마이페이지용)
     *
     * 주요 기능:
     * - 세션 사용자 ID 기반으로 본인이 작성한 상태 로그만 조회
     * - 필터 조건:
     * - type: ANSWER, FREE_SHARE, REGISTER 중 하나 (optional)
     * - hideHidden: 신고로 숨김 처리된 로그 제외 여부 (optional)
     * - page: 현재 페이지 번호 (1부터 시작)
     * - size: 페이지당 항목 수
     *
     * 동작 방식:
     * - 4가지 조건 조합에 따라 repository 메서드 동적으로 선택
     * - type 값이 유효하지 않으면 무시됨 (IllegalArgumentException catch)
     * - 정렬 기준은 createdAt 내림차순 (최신순)
     */
    public PageResult<StatusLogDto> getLogsByUser(
            Long userId, int page, int size, String type, boolean hideHidden) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Enum 변환 시 유효하지 않으면 null
        StatusType statusType = null;
        try {
            if (type != null && !type.isBlank()) {
                statusType = StatusType.valueOf(type);
            }
        } catch (IllegalArgumentException e) {
            // 무시하고 statusType은 null 유지
        }

        Page<StatusLog> pageResult;

        // 동적 조건 조합
        if (statusType != null && hideHidden) {
            pageResult = statusLogRepository
                    .findByReporter_IdAndStatusTypeAndIsHiddenFalse(userId, statusType, pageable);
        } else if (statusType != null) {
            pageResult = statusLogRepository
                    .findByReporter_IdAndStatusType(userId, statusType, pageable);
        } else if (hideHidden) {
            pageResult = statusLogRepository
                    .findByReporter_IdAndIsHiddenFalse(userId, pageable);
        } else {
            pageResult = statusLogRepository
                    .findByReporter_Id(userId, pageable);
        }

        List<StatusLogDto> dtoList = pageResult.getContent().stream()
                .map(StatusLogDto::fromEntity)
                .toList();

        return new PageResult<>(dtoList, pageResult.getTotalPages(), page);
    }

    /**
     * [3-4] 장소별 최근 로그 상태 조회 [미사용]
     * 최근 3시간 이내의 로그만 조회
     */
    public List<StatusLogDto> getLogsByPlace(Long placeId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        return statusLogRepository.findRecentByPlaceId(placeId, cutoff)
                .stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [3-5] 특정 장소의 가장 최근 공개된 상태 1건 조회 [미사용]
     * 마커 클릭 시 정보 표시용
     */
    public StatusLogDto getLatestVisibleLogByPlaceId(Long placeId) {
        StatusLog log = statusLogRepository.findTopByPlaceIdAndIsHiddenFalseOrderByCreatedAtDesc(placeId);
        return log != null ? StatusLogDto.fromEntity(log) : null;
    }

    // ─────────────────────────────────────────────
    // [5] 사용자 기능 (상태 로그 수정) UPDATE
    // ─────────────────────────────────────────────

    /**
     * StatusLogController: updateStatusLog
     * [5-1] 상태 로그 수정
     * 작성자 본인만 수정 가능
     */
    @Transactional
    public void updateStatusLog(Long logId, Long userId, StatusLogDto dto) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        if (!log.getReporter().getId().equals(userId)) {
            throw new IllegalStateException("해당 로그를 수정할 권한이 없습니다.");
        }

        // 수정 가능한 필드만 갱신
        log.setContent(dto.getContent());
        if (dto.getWaitCount() != null)
            log.setWaitCount(dto.getWaitCount());
        if (dto.getHasBathroom() != null)
            log.setHasBathroom(dto.getHasBathroom());
        if (dto.getMenuInfo() != null)
            log.setMenuInfo(dto.getMenuInfo());
        if (dto.getWeatherNote() != null)
            log.setWeatherNote(dto.getWeatherNote());
        if (dto.getVendorName() != null)
            log.setVendorName(dto.getVendorName());
        if (dto.getPhotoNote() != null)
            log.setPhotoNote(dto.getPhotoNote());
        if (dto.getNoiseNote() != null)
            log.setNoiseNote(dto.getNoiseNote());
        if (dto.getIsParkingAvailable() != null)
            log.setIsParkingAvailable(dto.getIsParkingAvailable());
        if (dto.getIsOpen() != null)
            log.setIsOpen(dto.getIsOpen());
        if (dto.getSeatCount() != null)
            log.setSeatCount(dto.getSeatCount());

        statusLogRepository.save(log);
    }

    /**
     * StatusLogController : selectAnswer
     * [5-2] 요청 기반 답변 채택 처리
     * - 요청 작성자가 본인 요청에 연결된 답변을 선택
     * - 상태 로그의 selected = true, 요청 마감 처리
     */
    @Transactional
    public void selectAnswer(Long statusLogId, Long loginUserId) {
        // [1] 답변 조회
        StatusLog log = statusLogRepository.findById(statusLogId)
                .orElseThrow(() -> new RuntimeException("답변 없음"));
        Request request = log.getRequest();

        // [2] 답변과 연결된 요청 확인
        if (request == null || !request.getUser().getId().equals(loginUserId)) {
            throw new RuntimeException("채택 권한이 없습니다.");
        }

        // [3] 이미 마감된 요청은 채택 불가
        if (request.isClosed()) {
            throw new RuntimeException("이미 마감된 요청입니다.");
        }

        try {

            // [4] 답변을 선택하고 자동으로 요청 마감 (연결된 Request)
            log.setSelected(true);
            statusLogRepository.save(log);

            // [5] 자동으로 연결된 요청 마감 처리
            request.setClosed(true);
            requestRepository.save(request);

            // [6] 포인트 지급/차감 처리 (트랜잭션)
            User answerer = log.getReporter(); // 답변 작성자
            User requester = request.getUser(); // 요청 작성자
            int points = request.getPoint();

            // [7] 요청자 포인트 차감 (요청 생성 시 지급한 포인트)
            pointService.givePoint(requester, -points, "답변 채택으로 포인트 차감", PointType.DEDUCT);

            // [8] 답변자 포인트 지급 (답변 채택 보상)
            pointService.givePoint(answerer, points, "답변 채택 보상", PointType.EARN);

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("다른 사용자에 의해 요청 정보가 변경되었습니다. 다시 시도해주세요.");
        }
    }

    // ─────────────────────────────────────────────
    // [6] 사용자 기능 (상태 로그 삭제) DELETE
    // ─────────────────────────────────────────────

    /**
     * StautsLogController: deleteStatusLog
     * [6-1] 상태 로그 삭제
     * - 작성자 본인만 삭제 가능
     */
    @Transactional
    public void deleteStatusLog(Long logId, Long userId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        if (!log.getReporter().getId().equals(userId)) {
            throw new IllegalStateException("해당 로그를 삭제할 권한이 없습니다.");
        }

        statusLogRepository.delete(log);
    }

    // ────────────────────────────────────────
    // [7] 관리자 기능
    // ────────────────────────────────────────

    /**
     * [7] 전체 상태 로그 조회 (관리자용)
     * - 모든 로그를 반환
     */
    public List<StatusLogDto> getAllLogs() {
        return statusLogRepository.findAll().stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

}