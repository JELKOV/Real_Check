package com.realcheck.place.dto;

import com.realcheck.place.entity.Place;
import com.realcheck.user.entity.User;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * PlaceDto 클래스
 * - 클라이언트 ↔ 서버 간 데이터 전달용 객체
 * - Place 엔티티와 달리 DB 매핑은 없고, 필요한 필드만 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceDto {

    private Long id; // 장소 ID

    @NotBlank(message = "장소 이름은 필수입니다.")
    @Size(max = 100, message = "장소 이름은 100자 이하로 입력해주세요.")
    private String name; // 장소 이름

    @NotBlank(message = "주소는 필수입니다.")
    private String address; // 도로명 주소

    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private double lat; // 위도

    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private double lng; // 경도

    @NotNull(message = "등록자(ownerId)는 필수입니다.")
    private Long ownerId; // 등록자 ID (User)

    /**
     * DTO → Entity 변환
     * - 등록 시 사용
     */
    public Place toEntity(User owner) {
        return Place.builder()
                .name(name)
                .address(address)
                .lat(lat)
                .lng(lng)
                .owner(owner)
                .build();
    }

    /**
     * Entity → DTO 변환
     * - 조회 시 사용
     */
    public static PlaceDto fromEntity(Place place) {
        return PlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .lat(place.getLat())
                .lng(place.getLng())
                .ownerId(place.getOwner() != null ? place.getOwner().getId() : null)
                .build();
    }
}
