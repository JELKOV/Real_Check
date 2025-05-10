package com.realcheck.status.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.realcheck.place.entity.Place;
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

@Data // 모든 필드에 대해 Getter, Setter, equals, hashCode, toString 메서드를 자동 생성
@NoArgsConstructor // 기본 생성자 (파라미터 없는 생성자) 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
@Builder
public class StatusLogDto {

    private Long id;
    private String content; // 대기 상황 설명 (예: "현재 3명 대기 중")
    private String imageUrl; // 이미지 URL (선택 사항)
    private boolean isSelected;

    private Long placeId; // 공식 장소 ID (null 가능)
    private Double lat; // 위치 위도 (커스텀 장소 대응)
    private Double lng; // 위치 경도

    private LocalDateTime createdAt; // 등록 일시
    private StatusType type;
    private Long requestId;

    private Long requestOwnerId;
    private String nickname;

    private String category;

    // 유연 필드들 (nullable 가능)
    private Boolean hasBathroom;
    private Integer waitCount;
    private String menuInfo;
    private String weatherNote;
    private String vendorName;
    private String photoNote;
    private String noiseNote;
    private Boolean isParkingAvailable;
    private Boolean isOpen;
    private Integer seatCount;

    /**
     * DTO → Entity 변환 메서드
     * - 컨트롤러/서비스에서 DTO를 받아 실제 DB에 저장할 수 있도록 Entity로 변환
     */
    public StatusLog toEntity(User user, Place place) {
        // 카테고리 필터링 (String → RequestCategory 변환)
        if (this.category != null) {
            RequestCategory categoryEnum = convertToCategory();
            filterFieldsByCategory(categoryEnum);
        }

        StatusLog log = new StatusLog();
        log.setContent(this.content);
        log.setImageUrl(this.imageUrl);
        log.setSelected(this.isSelected);
        log.setReporter(user);
        log.setPlace(place);
        log.setStatusType(this.type != null ? this.type : StatusType.ANSWER);

        if (place != null) {
            log.setLat(place.getLat());
            log.setLng(place.getLng());
        } else {
            log.setLat(this.lat);
            log.setLng(this.lng);
        }

        // 유연 필드 설정
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

        return log;
    }

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
                .placeId(log.getPlace() != null ? log.getPlace().getId() : null)
                .lat(log.getLat())
                .lng(log.getLng())
                .createdAt(log.getCreatedAt())
                .type(log.getStatusType())
                .requestId(log.getRequest() != null ? log.getRequest().getId() : null)
                .nickname(log.getReporter() != null ? log.getReporter().getNickname() : null)
                .requestOwnerId(
                        log.getRequest() != null && log.getRequest().getUser() != null
                                ? log.getRequest().getUser().getId()
                                : null)

                // 유연 필드 포함
                .category(
                        log.getRequest() != null && log.getRequest().getCategory() != null
                                ? log.getRequest().getCategory().name()
                                : null)

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

                .build();
    }

    /**
     * 카테고리 기반 필드 필터링 (RequestCategory로 수정)
     */
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
    }

    /**
     * 모든 유연 필드 초기화
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
    }
}
