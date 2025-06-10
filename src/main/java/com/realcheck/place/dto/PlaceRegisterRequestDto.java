package com.realcheck.place.dto;

import java.util.List;

import lombok.Data;

/**
 * 사용자 장소 등록 요청 DTO
 */
@Data
public class PlaceRegisterRequestDto {
    private String name;
    private String address;
    private double lat;
    private double lng;
    private List<String> categories;
}