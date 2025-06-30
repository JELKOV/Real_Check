package com.realcheck.status.service;

import com.realcheck.status.dto.PlaceLogGroupDto;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.common.dto.PageResult;
import com.realcheck.common.service.ViewTrackingService;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.point.entity.PointType;
import com.realcheck.point.service.PointService;
import com.realcheck.request.entity.Request;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.request.repository.RequestRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * StatusLogService (ALL DONE)
 * - 상태 등록, 수정, 조회 등 핵심 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
public class StatusLogService {

    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PointService pointService;
    private final RequestRepository requestRepository;
    private final ViewTrackingService viewTrackingService;

    // ─────────────────────────────────────────────
    // [1] 상태 로그 등록 (내부 로직) - CREATE
    // ─────────────────────────────────────────────

    /**
     * [1-1] 상태 로그 등록 내부 메서드 (공통 처리)
     * StatusLogService: register
     * StatusLogService: registerAnswer
     * StatusLogService: registerFreeShare
     * - 사용자 유효성 검사 (userId 존재 여부, 정지 여부 등)
     * - 장소 ID가 있는 경우 → 해당 장소 엔티티 조회
     * - 상태 로그 생성 (StatusLog.toEntity(user, place))
     * - 요청이 필요한 경우 → requestId로 요청 엔티티 연결
     * - 상태 로그 저장
     */
    private void registerInternal(Long userId, StatusLogDto dto, StatusType type) {

        // (1) 사용자 유효성 검사 (공통)
        User user = validateUser(userId);
        // (2) 장소 유효성 검사 (Optional)
        Place place = validatePlace(dto.getPlaceId());
        // (3) 공식 장소의 경우 허용된 요청 타입 확인 - 사용자 지정 장소이면 전체 타입 허용
        Request request = validateAndFilterFieldsByRequest(dto);

        // (4) 공식 장소일 경우 → 허용된 요청 카테고리인지 체크
        if (place != null) {
            validateAllowedRequestType(place, request);
        }

        // (5) DTO → Entity 변환 (place가 null이어도 toEntity 내부에서 request.getPlace()로 자동 보완됨)
        StatusLog log = dto.toEntity(user, place, request);

        // (6) 타입 및 요청 객체 설정 후 저장
        log.setStatusType(type);
        log.setRequest(request);
        statusLogRepository.save(log);
    }

    // ─────────────────────────────────────────────
    // [2] 사용자 기능 (상태 로그 등록) CREATE
    // ─────────────────────────────────────────────

    /**
     * [2-1] 요청 기반 답변 등록
     * AnswerController: createAnswer
     * - 누군가 등록한 요청(Request)에 대해 다른 사용자가 답변(상태 정보)을 등록할 때 사용
     * - 장소 정보는 optional (placeId가 null일 수 있음 – custom location 허용)
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
     * [2-2] 장소 기반 정보 공유 (공식 장소)
     * StatusLogController: register
     * - 요청 없이 해당 장소 관리자가 직접 공유
     * - 공식 등록된 장소(placeId 있음)
     */
    @Transactional
    public void register(Long userId, StatusLogDto dto) {

        // (1) 장소 소유자 확인
        validatePlaceOwnership(userId, dto.getPlaceId());

        // category가 존재하면 수동 필터링
        if (dto.getCategory() != null) {
            dto.filterFieldsByCategory(RequestCategory.valueOf(dto.getCategory()));
        }

        // (2) 상태 로그 등록 (공통 로직 호출)
        registerInternal(userId, dto, StatusType.REGISTER);
    }

    /**
     * [2-3] 자발적 정보 공유 등록 (FREE_SHARE)
     * StatusLogController: registerFreeShare
     * - 요청 없이 사용자가 직접 공유
     * - 조회수 기반 보상 구조로 활용 가능
     */
    @Transactional
    public void registerFreeShare(Long userId, StatusLogDto dto) {
        registerInternal(userId, dto, StatusType.FREE_SHARE);
    }

    /**
     * [2-4] 자발적 공유(FREE_SHARE): 조회수 증가 및 포인트 지급 [테스트용]
     * StatusLogController: viewFreeShare
     *
     * - 자발적 정보 공유 상태 로그를 조회할 때 호출됨
     * - 기본적으로 조회수는 항상 증가함 (Redis 연결 실패 시에도 예외 없이 처리)
     * - Redis 연결이 정상적일 경우에만 중복 조회 방지 로직(viewTrackingService)을 적용함
     * - 조회수 10 이상이 되면 최초 1회 포인트 보상 지급
     * - Redis가 없는 로컬 개발 환경에서도 테스트 가능하도록 유연하게 처리
     */
    // public StatusLogDto viewFreeShare(Long logId, Long userId) {
    // // [1] 상태 로그 조회
    // StatusLog log = statusLogRepository.findById(logId)
    // .orElseThrow(() -> new RuntimeException("해당 로그가 존재하지 않습니다."));

    // // [2] FREE_SHARE 타입인지 확인
    // if (log.getStatusType() != StatusType.FREE_SHARE) {
    // throw new RuntimeException("자발적 공유가 아닙니다.");
    // }
    // // [3] Redis 조회 제한 체크 - 기본 조회수 증가 허용
    // boolean allowIncrease = true;

    // // Redis 연결 가능할 경우만 어뷰징 방지 로직 적용
    // try {
    // System.out.println("[DEBUG] Redis 조회 제한 체크 시작 - userId: " + userId + ",
    // logId: " + logId);
    // // Redis에서 중복 조회 여부 확인 (false면 최근 조회했음 → 조회수 증가 불가)
    // allowIncrease = viewTrackingService.canIncreaseView(userId, logId); //
    // Redis에서 조회 제한 체크
    // System.out.println("[DEBUG] Redis 조회 제한 결과: " + allowIncrease);
    // } catch (RedisConnectionFailureException e) {
    // // Redis 연결 실패 시 예외 발생 → 조회수 증가 허용
    // // allowIncrease = true;
    // System.out.println("[Redis 실패] 조회 제한 없이 조회수 증가 허용 (logId=" + logId + ",
    // userId=" + userId
    // + " allowIncrease=" + allowIncrease + ")");
    // }

    // if (allowIncrease) {
    // log.setViewCount(log.getViewCount() + 1);

    // if (log.getViewCount() >= 10 && !log.isRewarded()) {
    // giveUserPoint(log.getReporter(), 10, "자발적 정보 조회수 보상");
    // log.setRewarded(true);
    // }

    // statusLogRepository.save(log);
    // }

    // return StatusLogDto.fromEntity(log);
    // }

    /**
     * [2-4] 자발적 공유(FREE_SHARE): 조회수 증가 및 포인트 지급 [배포용]
     * StatusLogController: viewFreeShare
     *
     * - 자발적 정보 공유 상태 로그의 상세 조회 시 호출됨
     * - Redis 연결이 필수이며, Redis 장애 발생 시 조회 자체를 차단 (예외 발생)
     * - Redis를 통해 중복 조회 여부를 확인하며, 제한 시간 내 중복 조회는 무시
     * - 중복이 아닐 경우에만 조회수 1 증가 및 포인트 지급 조건 확인
     * - 조회수 10 이상이고 보상이 아직 지급되지 않은 경우 포인트 10 지급
     * - 어뷰징 방지를 위해 조회 제한 로직은 반드시 Redis 기반으로 적용되어야 함
     */
    public StatusLogDto viewFreeShare(Long logId, Long userId) {
        // [1] 상태 로그 조회
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 로그가 존재하지 않습니다."));

        // [2] FREE_SHARE 타입인지 확인
        if (log.getStatusType() != StatusType.FREE_SHARE) {
            throw new RuntimeException("자발적 공유가 아닙니다.");
        }

        // [3] Redis 조회 제한 체크 (필수: 실패 시 조회수 증가 불가)
        boolean allowIncrease;

        try {
            allowIncrease = viewTrackingService.canIncreaseView(userId, logId);
            System.out.println(String.format(
                    "[PROD_LOG] Redis 조회 제한 여부: %s (userId=%d, logId=%d)",
                    allowIncrease, userId, logId));
        } catch (RedisConnectionFailureException e) {
            // Redis 필수 → 실패 시 조회 차단
            throw new IllegalStateException("Redis 서버 연결 실패: 조회수 증가 불가능", e);
        }

        // [4] 조회수 증가 및 포인트 처리
        if (allowIncrease) {
            log.setViewCount(log.getViewCount() + 1);

            if (log.getViewCount() >= 10 && !log.isRewarded()) {
                giveUserPoint(log.getReporter(), 10, "자발적 정보 조회수 보상");
                log.setRewarded(true);
            }

            statusLogRepository.save(log);
        } else {
            System.out.println(String.format(
                    "[PROD_LOG] Redis에 의해 조회수 증가 차단됨 (userId=%d, logId=%d)", userId, logId));
        }

        return StatusLogDto.fromEntity(log);
    }

    // ─────────────────────────────────────────────
    // [3] 사용자 기능 (상태 로그 조회) READ
    // ─────────────────────────────────────────────

    /**
     * [3-1] 현재 위치 기반 grouped 상태 로그 조회
     * StatusLogController: getGroupedLogs
     * - 사용자 위치(lat, lng)와 반경(radiusMeters)을 기준으로 3시간 이내 등록된 상태 로그 중
     * - 공식 장소(Place가 있는 경우)에 한하여 REGISTER + ANSWER 로그를 그룹핑하여 반환
     * - 장소(placeId) 기준으로 REGISTER는 가장 최신 1개만 포함, ANSWER는 전부 포함
     */
    @Transactional(readOnly = true)
    public List<PlaceLogGroupDto> findNearbyGroupedPlaceLogs(double lat, double lng, double radiusMeters) {
        // 현재 시각 기준 3시간 이내 로그만 조회
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);

        // 1. 3시간 이내, 반경 내 REGISTER + ANSWER 로그 전부 가져오기
        List<StatusLog> allLogs = statusLogRepository.findNearbyAnswerAndRegisterLogs(lat, lng, radiusMeters, cutoff);

        // 2. Place가 존재하는 로그만 필터링 → 공식 장소에 등록된 로그만 대상
        Map<Long, List<StatusLog>> grouped = allLogs.stream()
                .filter(log -> log.getPlace() != null)
                .collect(Collectors.groupingBy(log -> log.getPlace().getId())); // placeId 기준 그룹핑

        // 3. 그룹핑된 로그들에서 PlaceLogGroupDto로 변환 (REGISTER 1개 + ANSWER n개)
        return grouped.entrySet().stream()
                .map(entry -> {
                    List<StatusLog> logs = entry.getValue();

                    // 등록된 로그가 없는 경우는 skip
                    if (logs.isEmpty())
                        return null;

                    // 최신 REGISTER 로그 1개 추출
                    StatusLog latestRegister = logs.stream()
                            .filter(l -> l.getStatusType() == StatusType.REGISTER)
                            .max(Comparator.comparing(StatusLog::getCreatedAt))
                            .orElse(null);

                    // ANSWER 로그 전체 추출
                    List<StatusLog> answers = logs.stream()
                            .filter(l -> l.getStatusType() == StatusType.ANSWER)
                            .toList();

                    // Place 정보는 latestRegister가 null이면 다른 로그에서 가져옴
                    Place place = (latestRegister != null) ? latestRegister.getPlace() : logs.get(0).getPlace();

                    // DTO로 변환 후 반환
                    return PlaceLogGroupDto.builder()
                            .placeId(place.getId())
                            .placeName(place.getName())
                            .address(place.getAddress())
                            .latestRegister(latestRegister != null ? StatusLogDto.fromEntity(latestRegister) : null)
                            .answerLogs(answers.stream()
                                    .map(StatusLogDto::fromEntity)
                                    .toList())
                            .build();
                })
                .filter(Objects::nonNull) // null 제거 (logs.isEmpty() 방어용)
                .toList();
    }

    /**
     * [3-2] 일반 장소 요청 응답 로그 조회
     * StatusLogController: getNearbyUserLocationLogs
     * - 공식 장소(Place)와 연결되지 않은 ANSWER 로그만 필터링
     */
    @Transactional(readOnly = true)
    public Page<StatusLog> findNearbyUserLocationLogs(double lat, double lng, double radiusMeters, Pageable pageable) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        return statusLogRepository.findNearbyUserAnswerLogs(lat, lng, radiusMeters, cutoff, pageable);
    }

    /**
     * [3-3] 특정 요청에 대한 답변 리스트(상세 조회)
     * - StatusLogController: getAnswersByRequest
     */
    @Transactional(readOnly = true)
    public List<StatusLogDto> getAnswersByRequestId(Long requestId) {
        return statusLogRepository.findByRequestId(requestId).stream()
                .map(StatusLogDto::fromEntity)
                .toList();
    }

    /**
     * [3-4] 사용자별 전체 상태 로그 목록 조회 (마이페이지용)
     * StatusLogController: getMyStatusLogs
     * 
     * -주요 기능:
     * -- 세션 사용자 ID 기반으로 본인이 작성한 상태 로그만 조회
     * -- 필터 조건:
     * -- type: ANSWER, FREE_SHARE, REGISTER 중 하나 (optional)
     * -- hideHidden: 신고로 숨김 처리된 로그 제외 여부 (optional)
     * -- page: 현재 페이지 번호 (1부터 시작)
     * -- size: 페이지당 항목 수
     *
     * - 동작 방식:
     * -- 4가지 조건 조합에 따라 repository 메서드 동적으로 선택
     * -- type 값이 유효하지 않으면 무시됨 (IllegalArgumentException catch)
     * -- 정렬 기준은 createdAt 내림차순 (최신순)
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
                    .findByReporter_IdAndStatusTypeNotAndIsHiddenFalse(userId, StatusType.REGISTER, pageable);
        } else {
            pageResult = statusLogRepository
                    .findByReporter_IdAndStatusTypeNot(userId, StatusType.REGISTER, pageable);
        }

        List<StatusLogDto> dtoList = pageResult.getContent().stream()
                .map(StatusLogDto::fromEntity)
                .toList();

        return new PageResult<>(dtoList, pageResult.getTotalPages(), page);
    }

    /**
     * [3-5] 장소별 최근 로그 상태 조회
     * PageController: showCommunityPage
     * - ANSWER/REGISTER 둘다 조회
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
     * [3-6] 특정 장소의 가장 최근 공개된 공지로그 1건 조회
     * PageController: showCommunityPage
     * - REGISTER 공지로그
     */
    public StatusLogDto getLatestRegisterLogByPlaceId(Long placeId) {
        List<StatusLog> logs = statusLogRepository.findVisibleRegisterLogsByPlace(placeId);
        return logs.isEmpty() ? null : StatusLogDto.fromEntity(logs.get(0));
    }

    /**
     * [3-7] 특정 장소의 REGISTER 타입 공지 로그를 페이지 단위로 조회
     * PageController: showCommunityPage
     * - 공식 공지글 리스트를 페이지네이션 형식으로 제공
     * - 숨김 여부는 무시하고 전체 REGISTER 로그를 대상으로 함
     */
    public PageResult<StatusLogDto> getPagedRegisterLogsByPlace(Long placeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StatusLog> pageResult = statusLogRepository.findByPlaceIdAndStatusType(
                placeId, StatusType.REGISTER, pageable);

        List<StatusLogDto> dtoList = pageResult.getContent().stream()
                .map(StatusLogDto::fromEntity)
                .toList();

        return new PageResult<>(dtoList, pageResult.getTotalPages(), page);
    }

    /**
     * [3-8] 자발적 정보 공유(FREE_SHARE) 로그 조회
     * StatusLogController: getNearbyFreeShareLogs
     * - 현재 위치를 기준으로 반경 내 자발적 공유 상태 로그 조회
     * - 위도(lat), 경도(lng), 반경(radiusMeters) 파라미터 사용
     */
    public PageResult<StatusLogDto> findNearbyFreeShareLogs(
            double lat,
            double lng,
            double radiusMeters,
            LocalDateTime cutoff,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StatusLog> logs = statusLogRepository.findNearbyFreeShareLogs(lat, lng, radiusMeters, cutoff, pageable);

        List<StatusLogDto> dtos = logs.getContent().stream()
                .map(StatusLogDto::fromEntity)
                .toList();

        return new PageResult<>(dtos, logs.getTotalPages(), page);
    }

    // ─────────────────────────────────────────────
    // [4] 사용자 기능 (상태 로그 수정) UPDATE
    // ─────────────────────────────────────────────

    /**
     * [4-1] 상태 로그 수정
     * StatusLogController: updateStatusLog
     * - 작성자 본인만 수정 가능
     */
    @Transactional
    public void updateStatusLog(Long logId, Long userId, StatusLogDto dto) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        if (!log.getReporter().getId().equals(userId)) {
            throw new IllegalStateException("본인의 글만 수정할 수 있습니다.");
        }

        if (log.isSelected()) {
            throw new IllegalStateException("채택된 답변은 수정할 수 없습니다.");
        }

        if (log.getRequest() != null && log.getRequest().isClosed()) {
            throw new IllegalStateException("종료된 요청에 대한 응답은 수정할 수 없습니다.");
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
        if (dto.getCrowdLevel() != null)
            log.setCrowdLevel(dto.getCrowdLevel());
        if (dto.getExtra() != null)
            log.setExtra(dto.getExtra());

        log.setImageUrls(dto.getImageUrls());

        statusLogRepository.save(log);
    }

    /**
     * [4-2] 수정 가능한 상태 로그 조회
     * pageController: showEditForm
     * - 작성자 본인만 접근 가능
     * - REGISTER(공지) 타입만 수정 대상
     * 
     * @throws AccessDeniedException or IllegalArgumentException
     */
    @Transactional(readOnly = true)
    public StatusLogDto getEditableLog(Long logId, Long userId) {
        // 1. 로그 조회
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지를 찾을 수 없습니다."));

        // 2. 본인 확인
        if (!log.getReporter().getId().equals(userId)) {
            throw new AccessDeniedException("공지 수정은 작성자만 가능합니다.");
        }

        // 3. 공지 타입인지 확인
        if (log.getStatusType() != StatusType.REGISTER) {
            throw new IllegalArgumentException("해당 로그는 공지 형식이 아닙니다.");
        }

        // 4. DTO 변환 후 반환
        return StatusLogDto.fromEntity(log);
    }

    /**
     * [4-3] 요청 기반 답변 채택 처리
     * StatusLogController : selectAnswer
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
    // [5] 사용자 기능 (상태 로그 삭제) DELETE
    // ─────────────────────────────────────────────

    /**
     * [5-1] 상태 로그 삭제
     * StautsLogController: deleteStatusLog
     * - 작성자 본인만 삭제 가능
     */
    @Transactional
    public void deleteStatusLog(Long logId, Long userId) {
        StatusLog log = statusLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("해당 상태 정보가 존재하지 않습니다."));

        if (!log.getReporter().getId().equals(userId)) {
            throw new IllegalStateException("해당 로그를 삭제할 권한이 없습니다.");
        }

        // 공지가 아닌 경우에만 추가 제약
        if (log.getStatusType() != StatusType.REGISTER) {
            if (log.isSelected()) {
                throw new IllegalStateException("채택된 응답은 삭제할 수 없습니다.");
            }
            if (log.getRequest() != null && log.getRequest().isClosed()) {
                throw new IllegalStateException("종료된 요청에 대한 응답은 삭제할 수 없습니다.");
            }
        }

        statusLogRepository.delete(log);
    }

    // ────────────────────────────────────────
    // [6] 관리자 기능
    // ────────────────────────────────────────

    /**
     * [6-1] 사용자의 답변 등록 로그 반환 (관리자용)
     * UserAdminController: getUserDetails
     * - 특정 사용자가 작성한 상태 로그의 총 개수 조회
     */
    public long countLogsByUserId(Long userId) {
        return statusLogRepository.countByReporter_Id(userId);
    }

    // ────────────────────────────────────────
    // [*] 내부 공통 메서드
    // ────────────────────────────────────────

    /**
     * [1] 사용자 검증 (활성 사용자) - 공통 유효성 검사
     * StatusLogSerivce: registerInternal
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
     * [2] 장소 검증 (Optional) - 공통 유효성 검사
     * StatusLogSerivce: registerInternal
     */
    private Place validatePlace(Long placeId) {
        if (placeId == null)
            return null;
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소 없음"));
    }

    /**
     * [3] 요청 검증 및 필드 자동 필터링 - 공통 유효성 검사
     * StatusLogSerivce: registerInternal
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
     * [4] 허용된 요청 타입 검증 (공식 장소)
     * StatusLogSerivce: registerInternal
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

    /**
     * [5] 일일 등록 횟수 제한 확인
     * StatusLogService: registerAnswer
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
     * [6] 사용자가 동일 요청에 이미 답변을 등록했는지 확인
     * StatusLogService: registerAnswer
     */
    private boolean hasUserAnswered(Long requestId, Long userId) {
        return statusLogRepository.existsByRequestIdAndUserId(requestId, userId);
    }

    /**
     * [7] 장소 소유자 확인 메서드
     * StatusLogService: register
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
     * [8] 포인트 지급 처리 (FREE_SHARE에만 사용)
     * StatusLogService: viewFreeShare
     * - 자발적 정보 공유 상태 로그 조회 시 포인트 지급
     * - 조회수 10 이상이 되면 최초 1회 포인트 지급
     */
    private void giveUserPoint(User user, int points, String reason) {
        pointService.givePoint(user, points, reason, PointType.REWARD);
    }

}