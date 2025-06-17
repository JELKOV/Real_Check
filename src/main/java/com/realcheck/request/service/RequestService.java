package com.realcheck.request.service;

import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.AllowedRequestTypeRepository;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.point.entity.PointType;
import com.realcheck.point.service.PointService;
import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.entity.Request;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.request.repository.RequestRepository;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RequestService (ALL DONE)
 * - 요청(Request) 관련 비즈니스 로직 처리
 * - 요청 등록, 조회, 마감 처리 담당
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final PlaceRepository placeRepository;
    private final StatusLogRepository statusLogRepository;
    private final AllowedRequestTypeRepository allowedRequestTypeRepository;
    private final PointService pointService;

    // ─────────────────────────────────────────────
    // [1] 요청 등록 (Request 등록 로직)
    // ─────────────────────────────────────────────

    /**
     * [1-1] 요청 등록
     * RequestController: createRequest
     * - 유효성 검사
     * - 공식 장소 타입 검증
     */
    @Transactional
    public Request createRequest(RequestDto dto, User user) {
        // (1) 기본 유효성 검사
        validateRequestDto(dto);

        // (2) Place 확인 (공식 장소인 경우)
        Place place = null;
        if (dto.getPlaceId() != null) {
            place = placeRepository.findById(dto.getPlaceId())
                    .orElseThrow(() -> new IllegalArgumentException("공식 장소를 찾을 수 없습니다."));
        }
        // (3) 포인트 유효성 검사
        if (dto.getPoint() < 10) {
            throw new IllegalArgumentException("요청은 최소 10포인트 이상이어야 합니다.");
        }
        if (user.getPoints() < dto.getPoint()) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // (4) Request 엔티티로 변환
        Request request = dto.toEntity(user, place);

        // (5) 공식 장소일 경우 타입 검증
        if (place != null && !isValidForPlace(place, request.getCategory())) {
            throw new IllegalArgumentException("해당 장소에서는 선택한 요청 카테고리를 사용할 수 없습니다.");
        }

        // (6) 포인트 예치
        pointService.givePoint(user, -dto.getPoint(), "요청 등록 포인트 예치", PointType.RESERVE);

        // (7) 저장
        return requestRepository.save(request);
    }

    // ─────────────────────────────────────────────
    // [2] 요청 마감 처리 (수동 마감, 자동 마감)
    // ─────────────────────────────────────────────

    /**
     * [2-1] 요청자 요청취소 (스스로 마감처리)
     * RequestController: closeRequest
     */
    @Transactional
    public void closeRequest(Long requestId, Long userId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("요청 없음"));

        // 요청 마감 (이미 마감된 경우 예외 발생)
        if (request.isClosed()) {
            throw new RuntimeException("이미 마감된 요청입니다.");
        }

        // 수동 마감인 경우 사용자 권한 확인
        if (userId != null && !request.getUser().getId().equals(userId)) {
            throw new RuntimeException("해당 요청을 마감할 권한이 없습니다.");
        }

        // 요청 마감 처리
        request.setClosed(true);

        // 답변이 없고, 포인트도 아직 처리되지 않은 경우 → 환불
        int visibleAnswerCount = countVisibleStatusLogsByRequestId(requestId);
        if (visibleAnswerCount == 0 && !request.isPointHandled()) {
            pointService.givePoint(
                    request.getUser(),
                    request.getPoint(),
                    "답변 없음 - 포인트 환불",
                    PointType.REFUND);
            request.setPointHandled(true);
            // 환불 여부 체크
            request.setRefundProcessed(true);
        }

        // 동시성 문제 고려
        try {
            requestRepository.save(request);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("동일한 요청을 동시에 마감하려 했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * [2-2] 자동 마감 대상 조회
     * AutoCloseRequestService: autoCloseExpiredRequests
     * - 조건:
     * - 1. 마감되지 않았고 (isClosed = false)
     * - 2. 생성 시점이 기준(threshold)보다 이전이며
     * - 3. 숨김 처리되지 않은 답변이 1개 이상 존재하는 요청
     */
    @Transactional(readOnly = true)
    public List<Request> findOpenRequestsWithAnswers(LocalDateTime threshold) {
        return requestRepository.findAllVisibleStatusLogsAfterThreshold(threshold);
    }

    /**
     * [2-3] 요청 저장 메서드
     * AutoCloseRequestService: autoCloseExpiredRequests
     * - 자동 마감 시 상태 변경 저장용
     */
    @Transactional
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    // ─────────────────────────────────────────────
    // [3] 요청 조회 (단건 조회, 리스트 조회)
    // ─────────────────────────────────────────────

    /**
     * [3-1] 요청 단건 조회 (ID로 조회)
     * RequestController: findRequestById
     */
    @Transactional
    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    /**
     * [3-2] 지역 기반 요청이 3시간이 지나서 오픈된 요청 조회
     * RequestController: findOpenRequests
     * - 미마감
     * - 답변 3개 미만
     * - 3시간 지난 요청
     */
    public List<RequestDto> findOpenRequests(int page, int size, double lat, double lng, double radius,
            String category) {
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime threshold = LocalDateTime.now().minusHours(3);
        RequestCategory categoryEnum = parseCategory(category);

        // Repository에서 필터링된 요청 조회
        Page<Request> entities = requestRepository.findOpenRequestsWithLocation(
                lat, lng, radius, threshold, categoryEnum, pageable);

        // DTO로 변환하여 반환
        return entities.stream()
                .map(r -> {
                    int visibleCount = countVisibleStatusLogsByRequestId(r.getId());
                    return RequestDto.fromEntity(r, visibleCount);
                })
                .toList();
    }

    /**
     * [3-2-A] 카테고리 파싱 (String → Enum)
     */
    private RequestCategory parseCategory(String category) {
        if (category == null || category.isBlank())
            return null;
        try {
            return RequestCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 카테고리 값: " + category);
        }
    }

    /**
     * [3-3] 최신 요청 조회 (현재 사용자 위치 기준 / 지도 반경 기반)
     * RequestController: findNearbyOpenRequests
     * - 위도, 경도 기준으로 radius(m) 이내
     * - 장소 좌표가 존재하며 답변 수가 3개 미만인 요청 필터
     * - TODO 3시간 이내 수정 (테스트라 48시간으로 바꿈)
     */
    public List<RequestDto> findNearbyValidRequests(double lat, double lng, double radiusMeters) {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(48);
        List<Request> requests = requestRepository.findNearbyValidRequests(lat, lng, radiusMeters, timeLimit);
        return requests.stream()
                .map(r -> {
                    int visibleCount = countVisibleStatusLogsByRequestId(r.getId());
                    return RequestDto.fromEntity(r, visibleCount);
                })
                .toList();
    }

    /**
     * [3-4] 특정 사용자(userId)의 요청 목록 조회 (페이지네이션 + 필터 포함)
     * RequestController.findMyRequests
     * - 카테고리, 키워드, 페이지 정보에 따라 사용자 요청 목록을 조회하고 DTO로 변환하여 반환
     */
    public Page<RequestDto> findMyRequests(Long userId, RequestCategory category, String keyword, int page, int size) {
        // [1] 페이지 정보 설정 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // [2] repository에 동적 필터 메서드 호출 (카테고리/키워드 필터링 포함)
        Page<Request> entities = requestRepository.findMyRequestsWithFilters(userId, category, keyword, pageable);

        // [3] Entity → DTO 변환 + visibleAnswerCount 계산 포함
        return entities.map(r -> {
            int visibleCount = countVisibleStatusLogsByRequestId(r.getId()); // 비공개 제외한 답변 수
            return RequestDto.fromEntity(r, visibleCount);
        });
    }

    /**
     * [3-5] 장소 ID 기준으로 요청 목록 조회
     * PageController: showCommunityPage
     */
    public List<RequestDto> getRequestsByPlaceId(Long placeId) {
        List<Request> requests = requestRepository.findByPlaceIdOrderByCreatedAtDesc(placeId);
        return requests.stream()
                .map(r -> {
                    int visibleCount = countVisibleStatusLogsByRequestId(r.getId());
                    return RequestDto.fromEntity(r, visibleCount);
                })
                .toList();
    }

    // ────────────────────────────────────────
    // [*] 내부 공통 메서드
    // ────────────────────────────────────────

    /**
     * [1] 기본 요청 유효성 검사
     * RequestService: createRequest
     */
    private void validateRequestDto(RequestDto dto) {
        if (dto.getCategory() == null) {
            throw new IllegalArgumentException("질문의 카테고리를 선택해주세요.");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("질문의 제목을 입력해주세요.");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new IllegalArgumentException("질문의 내용을 입력해주세요.");
        }
    }

    /**
     * [2] 공식 장소 타입 검증 로직 / 지정된 장소(Place)에 허용된 요청 타입인지 확인 / 사용자가 지정한 장소는 무조건 허용
     * RequestService: createRequest
     */
    public boolean isValidForPlace(Place place, RequestCategory category) {
        // (1) 사용자 지정 장소인 경우 (place == null) → 항상 허용
        if (place == null) {
            return true;
        }

        // (2) 공식 장소일 경우 → Repository 직접 검증
        return allowedRequestTypeRepository.existsByPlaceIdAndRequestType(place.getId(), category);
    }

    /**
     * [3] 숨김 처리되지 않은 상태 로그(답변) 수 조회
     * RequestService: findNearbyValidRequests
     * RequestService: findOpenRequests
     * RequestSerivce: getRequestsByPlaceId
     * RequestService: closeRequest
     * RequestController: findRequestById
     * RequestController: findMyRequests
     */
    public int countVisibleStatusLogsByRequestId(Long requestId) {
        return (int) statusLogRepository.countByRequestIdAndIsHiddenFalse(requestId);
    }

}
