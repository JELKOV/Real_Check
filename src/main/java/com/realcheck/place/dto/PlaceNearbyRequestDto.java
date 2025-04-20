package com.realcheck.place.dto;

import lombok.Data;

/**
 * 현재 위치 기반 근처 장소 조회 요청 DTO
 */

@Data
public class PlaceNearbyRequestDto {
    private double latitude; // 현재 위도
    private double longitude; // 현재 경도
    private double radius; // 탐색 반경(단위: km)
}
