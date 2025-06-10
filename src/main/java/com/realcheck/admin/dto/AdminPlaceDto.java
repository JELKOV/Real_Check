package com.realcheck.admin.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPlaceDto {
    /** 장소 식별자 */
    private Long id;
    /** 장소 이름 */
    private String name;
    /** 도로명 주소 */
    private String address;
    /** 위도 */
    private double lat;
    /** 경도 */
    private double lng;

    /** 관리자 승인 여부 */
    private boolean approved;
    private boolean rejected;

    public boolean isPending() {
        return !approved && !rejected;
    }

    /** 등록자 (owner) ID */
    private Long ownerId;
    /** 등록자 이름 */
    private String ownerName;
    /** 최초 등록 시각 */
    private LocalDateTime createdAt;
    /** 마지막 수정 시각 */
    private LocalDateTime updatedAt;

    /**
     * Entity → DTO 변환 헬퍼
     */
    public static AdminPlaceDto fromEntity(
            com.realcheck.place.entity.Place place,
            String ownerName) {
        return AdminPlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .lat(place.getLat())
                .lng(place.getLng())
                .approved(place.isApproved())
                .rejected(place.isRejected())
                .ownerId(place.getOwner() != null ? place.getOwner().getId() : null)
                .ownerName(ownerName)
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}
