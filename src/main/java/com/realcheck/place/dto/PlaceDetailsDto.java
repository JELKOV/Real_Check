package com.realcheck.place.dto;

import com.realcheck.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PlaceDetailsDto 클래스
 * - 장소 상세 정보 전달용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailsDto {
    private String fullAddress;
    private String recentInfo;
    private String communityLink;

    /**
     * Entity → DetailsDTO 변환
     */
    public static PlaceDetailsDto fromEntity(Place place) {
        return new PlaceDetailsDto(
                place.getAddress(),
                place.getRecentInfo(),
                place.getCommunityLink()
        );
    }
}

