package com.realcheck.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.realcheck.place.entity.Place;
import com.realcheck.request.entity.Request;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.user.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RequestDto
 * - 클라이언트 ↔ 서버 간 요청 정보를 주고받기 위한 DTO 클래스
 * - Request 엔티티와 1:1 매핑되며, 유연 필드 포함
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {

    // ────────────────────────────────────────
    // [1] 기본 필드
    // ────────────────────────────────────────

    // 요청 ID (PK)
    private Long id;
    // 요청 제목
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    // 요청 상세 설명
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    // 요청에 걸린 포인트
    @NotNull(message = "포인트는 필수입니다.")
    private Integer point;

    // 요청 카테고리
    private RequestCategory category;

    // ────────────────────────────────────────
    // [2] 장소 관련 필드
    // ────────────────────────────────────────

    // 사용자 입력장소
    private String customPlaceName;
    // 공식 장소 ID
    private Long placeId;
    // 공식 장소 이름
    private String placeName;
    // 위도
    private Double lat;
    // 경도
    private Double lng;

    // ────────────────────────────────────────
    // [3] 상태 필드
    // ────────────────────────────────────────

    // 요청 마감 여부
    private boolean isClosed;

    // 요청 등록 시각
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 요청 수정 시각
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ────────────────────────────────────────
    // [4] 요청자 정보
    // ────────────────────────────────────────

    // 요청자 PK
    private Long requesterId;
    // 요청자 이메일
    private String requesterEmail;
    // 요청자 닉네임
    private String requesterNickname;

    // ────────────────────────────────────────
    // [5] 답변 관련
    // ────────────────────────────────────────

    // 해당 요청에 달린 답변 갯수
    private int answerCount;

    // ────────────────────────────────────────
    // [6] 카테고리별 유연 필드 (12개)
    // ────────────────────────────────────────

    // 카테고리별 확장 필드
    private Integer waitCount; // WAITING_STATUS
    private Integer crowdLevel; // CROWD_LEVEL
    private Boolean hasBathroom; // BATHROOM
    private String menuInfo; // FOOD_MENU
    private String weatherNote; // WEATHER_LOCAL
    private String vendorName; // STREET_VENDOR
    private String photoNote; // PHOTO_REQUEST
    private String noiseNote; // NOISE_LEVEL
    private Boolean isParkingAvailable; // PARKING
    private Boolean isOpen; // BUSINESS_STATUS
    private Integer seatCount; // OPEN_SEAT
    private String extra; // ETC (기타)

    // ────────────────────────────────────────
    // [7] DTO → Entity 변환 메서드 - 클라이언트가 보낸 데이터를 실제 DB에 저장 가능한 형태로 변환
    // ────────────────────────────────────────
    public Request toEntity(User user, Place place) {
        // 장소 검증 로직
        if (placeId != null) { // 공식 장소 선택
            if (place == null || lat == null || lng == null) {
                throw new IllegalArgumentException("공식 장소 정보가 누락되었습니다.");
            }
        } else if (customPlaceName != null) { // 사용자 지정 장소
            if (lat == null || lng == null) {
                throw new IllegalArgumentException("사용자 지정 장소의 좌표가 누락되었습니다.");
            }
        } else {
            throw new IllegalArgumentException("장소 정보가 선택되지 않았습니다.");
        }

        return Request.builder()
                .category(category)
                .user(user)
                .title(title)
                .place(place)
                .content(content)
                .point(point)
                .customPlaceName(customPlaceName)
                .lat(lat)
                .lng(lng)
                .isClosed(false)

                // 카테고리별 동적 필드
                .waitCount(waitCount)
                .crowdLevel(crowdLevel)
                .hasBathroom(hasBathroom)
                .menuInfo(menuInfo)
                .weatherNote(weatherNote)
                .vendorName(vendorName)
                .photoNote(photoNote)
                .noiseNote(noiseNote)
                .isParkingAvailable(isParkingAvailable)
                .isOpen(isOpen)
                .seatCount(seatCount)
                .extra(extra)

                .build();
    }

    // ────────────────────────────────────────
    // [8] Entity → DTO 변환 메서드 - DB에서 꺼낸 Request 객체를 클라이언트에게 전달 가능한 형태로 변환
    // ────────────────────────────────────────
    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .content(r.getContent())
                .point(r.getPoint())
                .customPlaceName(r.getCustomPlaceName())
                .placeId(r.getPlace() != null ? r.getPlace().getId() : null)
                .placeName(r.getPlace() != null ? r.getPlace().getName() : null)
                .lat(r.getLat())
                .lng(r.getLng())
                .category(r.getCategory())
                .isClosed(r.isClosed())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .requesterId(r.getUser() != null ? r.getUser().getId() : null)
                .requesterEmail(r.getUser() != null ? r.getUser().getEmail() : null)
                .requesterNickname(r.getUser() != null ? r.getUser().getNickname() : null)
                .answerCount(r.getStatusLogs() != null ? r.getStatusLogs().size() : 0)

                // 카테고리별 동적 필드
                .waitCount(r.getWaitCount())
                .crowdLevel(r.getCrowdLevel())
                .hasBathroom(r.getHasBathroom())
                .menuInfo(r.getMenuInfo())
                .weatherNote(r.getWeatherNote())
                .vendorName(r.getVendorName())
                .photoNote(r.getPhotoNote())
                .noiseNote(r.getNoiseNote())
                .isParkingAvailable(r.getIsParkingAvailable())
                .isOpen(r.getIsOpen())
                .seatCount(r.getSeatCount())
                .extra(r.getExtra())

                .build();
    }
}
