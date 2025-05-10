package com.realcheck.place.dto;

import com.realcheck.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * PlaceDetailsDto 클래스
 * - 장소 상세 정보 전달용 DTO
 * - 허용된 요청 타입 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailsDto {
    private Long id;
    private String name;
    private String address;
    private double lat;
    private double lng;
    private boolean isApproved;
    private String recentInfo;
    private String communityLink;
    private Set<String> allowedRequestTypes;

    /**
     * Entity → DetailsDTO 변환
     */
    public static PlaceDetailsDto fromEntity(Place place, Set<String> allowedRequestTypes) {
        return new PlaceDetailsDto(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getLat(),
                place.getLng(),
                place.isApproved(),
                place.getRecentInfo(),
                place.getCommunityLink(),
                allowedRequestTypes);
    }
}
