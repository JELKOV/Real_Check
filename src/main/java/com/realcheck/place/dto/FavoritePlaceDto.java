package com.realcheck.place.dto;

import com.realcheck.place.entity.FavoritePlace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoritePlaceDto {
    private Long placeId;
    private String placeName;
    private String address;
    private boolean approved;

    public static FavoritePlaceDto fromEntity(FavoritePlace fav) {
        return FavoritePlaceDto.builder()
                .placeId(fav.getPlace().getId())
                .placeName(fav.getPlace().getName())
                .address(fav.getPlace().getAddress())
                .approved(fav.getPlace().isApproved())
                .build();
    }

}
