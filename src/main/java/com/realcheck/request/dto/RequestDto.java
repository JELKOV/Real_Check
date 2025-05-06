package com.realcheck.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private RequestCategory category;

    // 유저가 직접 입력한 장소 이름 (ex. "문방구 앞 붕어빵")
    private String customPlaceName;
    // 등록된 Place 객체의 이름 (공식 장소 이름)
    private String placeName;
    // 위도 / 경도 정보 (지도에 표시하기 위함)
    private Double lat;
    private Double lng;

    // 요청 마감 여부 (답변 채택 시 true)
    private boolean isClosed;

    // 요청 등록 시각
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 요청자 PK/ 이메일 / 닉네임
    private Long requesterId;
    private String requesterEmail;
    private String requesterNickname;

    // 해당 요청에 달린 답변 수
    private int answerCount;

    // 카테고리별 확장 필드
    private Integer waitCount; // WAITING_STATUS, CROWD_LEVEL
    private Boolean hasBathroom; // BATHROOM
    private String menuInfo; // FOOD_MENU
    private String weatherNote; // WEATHER_LOCAL
    private String vendorName; // STREET_VENDOR
    private String photoNote; // PHOTO_REQUEST
    private String noiseNote; // NOISE_LEVEL
    private Boolean isParkingAvailable; // PARKING
    private Boolean isOpen; // BUSINESS_STATUS
    private Integer seatCount; // OPEN_SEAT
    private String extra; // ETC or 확장 JSON

    /**
     * DTO → Entity 변환 메서드
     * - 클라이언트가 보낸 데이터를 실제 DB에 저장 가능한 형태로 변환
     */
    public Request toEntity(User user) {
        if (placeName == null && (customPlaceName == null || lat == null || lng == null)) {
            throw new IllegalArgumentException("장소 정보가 누락되었습니다.");
        }

        return Request.builder()
                .category(category)
                .user(user)
                .title(title)
                .content(content)
                .point(point)
                .customPlaceName(customPlaceName)
                .lat(lat)
                .lng(lng)
                .createdAt(LocalDateTime.now())
                .isClosed(false)

                .waitCount(waitCount)
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

    /**
     * Entity → DTO 변환 메서드
     * - DB에서 꺼낸 Request 객체를 클라이언트에게 전달 가능한 형태로 변환
     */
    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .content(r.getContent())
                .point(r.getPoint())
                .customPlaceName(r.getCustomPlaceName())
                .placeName(r.getPlace() != null ? r.getPlace().getName() : null)
                .lat(r.getLat())
                .lng(r.getLng())
                .category(r.getCategory())
                .isClosed(r.isClosed())
                .createdAt(r.getCreatedAt())
                .requesterId(r.getUser() != null ? r.getUser().getId() : null)
                .requesterEmail(r.getUser() != null ? r.getUser().getEmail() : null)
                .requesterNickname(r.getUser() != null ? r.getUser().getNickname() : null)
                .answerCount(r.getStatusLogs() != null ? r.getStatusLogs().size() : 0)

                .waitCount(r.getWaitCount())
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
