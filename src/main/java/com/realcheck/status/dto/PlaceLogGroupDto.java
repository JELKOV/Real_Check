package com.realcheck.status.dto;

import com.realcheck.status.entity.StatusLog;
import com.realcheck.place.entity.Place;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceLogGroupDto {

    private Long placeId;
    private String placeName;
    private String address;

    private StatusLogDto latestRegister; // 공지 (최신 1개)
    private List<StatusLogDto> answerLogs; // 응답들

    public static PlaceLogGroupDto from(Place place, StatusLog latestRegister, List<StatusLog> answers) {
        return PlaceLogGroupDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .latestRegister(StatusLogDto.fromEntity(latestRegister))
                .answerLogs(answers.stream()
                        .map(StatusLogDto::fromEntity)
                        .toList())
                .build();
    }
}
