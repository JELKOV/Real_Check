package com.realcheck.status.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.realcheck.place.entity.Place;
import com.realcheck.request.entity.Request;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.user.entity.User;

import lombok.*;

/**
 * StatusLogDto 클래스
 * - 클라이언트 ↔ 서버 간 대기 정보(StatusLog)를 전달할 때 사용하는 데이터 전송 객체 (DTO)
 * - Entity와는 다르게 DB와 직접 연결되지 않으며, 필요한 데이터만 추려서 담는다
 * - 주로 컨트롤러와 서비스 간 전달, 응답 포맷 구성에 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusLogDto {

    // 동시성 제어용 버전 필드 추가
    private Integer version;

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (ID, 내용, 이미지, 선택 여부)
    // ─────────────────────────────────────────────

    private Long id;
    // 대기 상황 설명 (예: "현재 3명 대기 중")
    private String content;
    // 이미지 URL (선택 사항)
    private String imageUrl;
    // 답변 채택 여부 (요청 답변일 경우)
    private boolean isSelected;
    // 신고 횟수
    private int reportCount;
    // 숨김 표시
    private boolean isHidden;

    // ─────────────────────────────────────────────
    // [2] 사용자 및 위치 정보
    // ─────────────────────────────────────────────

    // 답변 작성자
    private Long userId;
    // 답변 작성자 닉네임 (익명 가능)
    private String nickname;

    // 공식 장소 ID (null 가능)
    private Long placeId;
    // 장소 이름
    private String placeName;
    // 사용자 지정 장소 이름
    private String customPlaceName;
    // 장소 위도
    private Double lat;
    // 장소 경도
    private Double lng;

    // 연결된 요청 ID (답변형일 경우)
    private Long requestId;
    // 요청 작성자 ID (답변 채택용)
    private Long requestOwnerId;
    // 요청 마감 여부
    private boolean isRequestClosed;
    // 요청 제목
    private String requestTitle;
    // 요청 내역
    private String requestContent;

    // 상태 타입 (ANSWER, FREE_SHARE 등)
    private StatusType type;
    // 등록 일시
    private LocalDateTime createdAt;
    // 업데이트 일시
    private LocalDateTime updatedAt;

    // 요청 카테고리 (12가지 중 하나, String)
    private String category;

    // ─────────────────────────────────────────────
    // [3] 유연 필드 (카테고리별 동적 사용)
    // 각 카테고리에 따라 동적으로 사용됨
    // ─────────────────────────────────────────────

    // 화장실 여부 (BATHROOM)
    private Boolean hasBathroom;
    // 메뉴 정보 (FOOD_MENU)
    private String menuInfo;
    // 대기 인원 (WAITING_STATUS, CROWD_LEVEL)
    private Integer waitCount;
    // 날씨 상태 (WEATHER_LOCAL)
    private String weatherNote;
    // 노점 이름 (STREET_VENDOR)
    private String vendorName;
    // 사진 요청 메모 (PHOTO_REQUEST)
    private String photoNote;
    // 소음 상태 (NOISE_LEVEL)
    private String noiseNote;
    // 주차 가능 여부 (PARKING)
    private Boolean isParkingAvailable;
    // 영업 여부 (BUSINESS_STATUS)
    private Boolean isOpen;
    // 남은 좌석 수 (OPEN_SEAT)
    private Integer seatCount;
    // 혼잡도 (CROWD_LEVEL)
    private Integer crowdLevel;
    // 기타 정보 (ETC) - 사용자 자유 입력
    private String extra;

    // ─────────────────────────────────────────────
    // [4] DTO → Entity 변환 메서드
    // ─────────────────────────────────────────────

    /**
     * DTO → Entity 변환 메서드
     * - StatusLogDto 데이터를 기반으로 실제 DB에 저장할 StatusLog 엔티티 객체 생성
     * - 요청(Request)이 존재하고 place가 null일 경우, 요청에 연결된 place를 자동 설정
     * - 유연 필드(category 기반 필드)는 요청 카테고리를 기준으로 불필요한 필드 제거 후 설정
     */
    public StatusLog toEntity(User user, Place place, Request request) {
        
        // (1) 요청 카테고리 정보가 있으면 해당 카테고리에 맞게 유연 필드 필터링
        if (this.category != null) {
            RequestCategory categoryEnum = convertToCategory(); // String → Enum
            filterFieldsByCategory(categoryEnum); // 해당 카테고리 외 필드는 null 처리
        }

        // (2) place가 null인데 request로부터 연결된 place가 존재할 경우 자동 설정
        if (place == null && request != null && request.getPlace() != null) {
            place = request.getPlace();
        }

        // (3) StatusLog 엔티티 객체 생성
        StatusLog log = new StatusLog();
        log.setContent(this.content);
        log.setImageUrl(this.imageUrl);
        log.setSelected(this.isSelected);
        log.setReporter(user);
        log.setPlace(place);
        log.setStatusType(this.type != null ? this.type : StatusType.ANSWER); // 기본값: ANSWER

        // (4) 좌표 정보 설정 (공식 장소가 있으면 place로부터, 없으면 수동 입력)
        if (place != null) {
            log.setLat(place.getLat());
            log.setLng(place.getLng());
        } else {
            log.setLat(this.lat);
            log.setLng(this.lng);
        }

        // (5) 유연 필드 설정 (필요한 필드만 유지되어 있음)
        log.setWaitCount(this.waitCount);
        log.setHasBathroom(this.hasBathroom);
        log.setMenuInfo(this.menuInfo);
        log.setWeatherNote(this.weatherNote);
        log.setVendorName(this.vendorName);
        log.setPhotoNote(this.photoNote);
        log.setNoiseNote(this.noiseNote);
        log.setIsParkingAvailable(this.isParkingAvailable);
        log.setIsOpen(this.isOpen);
        log.setSeatCount(this.seatCount);
        log.setCrowdLevel(this.crowdLevel);
        log.setExtra(extra);

        return log;
    }

    // ─────────────────────────────────────────────
    // [5] Entity → DTO 변환 메서드
    // ─────────────────────────────────────────────

    /**
     * Entity → DTO 변환 메서드
     * - DB에 저장된 StatusLog 엔티티를 클라이언트에 응답할 DTO 형태로 변환
     */
    public static StatusLogDto fromEntity(StatusLog log) {
        return StatusLogDto.builder()
                .id(log.getId())
                .content(log.getContent())
                .isSelected(log.isSelected())
                .imageUrl(log.getImageUrl())
                .reportCount(log.getReportCount())
                .isHidden(log.isHidden())
                .userId(log.getReporter() != null ? log.getReporter().getId() : null)
                .placeId(log.getPlace() != null ? log.getPlace().getId() : null)
                .placeName(log.getPlace() != null ? log.getPlace().getName() : null)
                .customPlaceName(
                        log.getPlace() == null && log.getRequest() != null
                                ? log.getRequest().getCustomPlaceName()
                                : null)
                .lat(log.getLat())
                .lng(log.getLng())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .type(log.getStatusType())
                .requestId(log.getRequest() != null ? log.getRequest().getId() : null)
                .isRequestClosed(log.getRequest() != null && log.getRequest().isClosed())
                .nickname(log.getReporter() != null ? log.getReporter().getNickname() : null)
                .requestOwnerId(
                        log.getRequest() != null && log.getRequest().getUser() != null
                                ? log.getRequest().getUser().getId()
                                : null)
                .requestTitle(log.getRequest() != null ? log.getRequest().getTitle() : null)
                .requestContent(log.getRequest() != null ? log.getRequest().getContent() : null)

                // 유연 필드 포함
                .category(
                        log.getRequest() != null && log.getRequest().getCategory() != null
                                ? log.getRequest().getCategory().name()
                                : null)

                // 유연 필드 설정
                .hasBathroom(log.getHasBathroom())
                .waitCount(log.getWaitCount())
                .menuInfo(log.getMenuInfo())
                .weatherNote(log.getWeatherNote())
                .vendorName(log.getVendorName())
                .photoNote(log.getPhotoNote())
                .noiseNote(log.getNoiseNote())
                .isParkingAvailable(log.getIsParkingAvailable())
                .isOpen(log.getIsOpen())
                .seatCount(log.getSeatCount())
                .crowdLevel(log.getCrowdLevel())
                .extra(log.getExtra())

                .version(log.getVersion())

                .build();
    }

    // ─────────────────────────────────────────────
    // [6] 카테고리 기반 필드 필터링
    // ─────────────────────────────────────────────

    public void filterFieldsByCategory(RequestCategory category) {
        if (category == null) {
            clearAllFlexibleFields();
            return;
        }

        switch (category) {
            case WAITING_STATUS -> clearExcept("waitCount");
            case BATHROOM -> clearExcept("hasBathroom");
            case FOOD_MENU -> clearExcept("menuInfo");
            case WEATHER_LOCAL -> clearExcept("weatherNote");
            case STREET_VENDOR -> clearExcept("vendorName");
            case PHOTO_REQUEST -> clearExcept("photoNote");
            case NOISE_LEVEL -> clearExcept("noiseNote");
            case PARKING -> clearExcept("isParkingAvailable");
            case BUSINESS_STATUS -> clearExcept("isOpen");
            case OPEN_SEAT -> clearExcept("seatCount");
            case CROWD_LEVEL -> clearExcept("crowdLevel");
            case ETC -> clearExcept("extra");
            default -> clearAllFlexibleFields();
        }
    }

    /**
     * 카테고리 변환 (String → RequestCategory)
     */
    private RequestCategory convertToCategory() {
        try {
            return RequestCategory.valueOf(this.category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 카테고리: " + this.category);
        }
    }

    /**
     * 유연 필드 초기화 (필요한 필드만 유지)
     */
    private void clearExcept(String... keepFields) {
        List<String> keepList = List.of(keepFields);
        if (!keepList.contains("waitCount"))
            this.waitCount = null;
        if (!keepList.contains("hasBathroom"))
            this.hasBathroom = null;
        if (!keepList.contains("menuInfo"))
            this.menuInfo = null;
        if (!keepList.contains("weatherNote"))
            this.weatherNote = null;
        if (!keepList.contains("vendorName"))
            this.vendorName = null;
        if (!keepList.contains("photoNote"))
            this.photoNote = null;
        if (!keepList.contains("noiseNote"))
            this.noiseNote = null;
        if (!keepList.contains("isParkingAvailable"))
            this.isParkingAvailable = null;
        if (!keepList.contains("isOpen"))
            this.isOpen = null;
        if (!keepList.contains("seatCount"))
            this.seatCount = null;
        if (!keepList.contains("crowdLevel"))
            this.crowdLevel = null;
        if (!keepList.contains("extra"))
            this.extra = null;
    }

    /**
     * 모든 유연 필드 초기화 (12개 필드 전체)
     */
    private void clearAllFlexibleFields() {
        this.waitCount = null;
        this.hasBathroom = null;
        this.menuInfo = null;
        this.weatherNote = null;
        this.vendorName = null;
        this.photoNote = null;
        this.noiseNote = null;
        this.isParkingAvailable = null;
        this.isOpen = null;
        this.seatCount = null;
        this.crowdLevel = null;
        this.extra = null;
    }
}
