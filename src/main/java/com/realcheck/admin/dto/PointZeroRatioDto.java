package com.realcheck.admin.dto;

/**
 * 잔액이 0원인 사용자 비율을 나타내는 DTO.
 * - zeroCount: 잔액이 0원인 사용자 수
 * - totalCount: 전체 사용자 수
 * - zeroRatio: 잔액이 0원인 사용자의 비율 (0 ~ 1 사이)
 */
public record PointZeroRatioDto(
        long zeroCount,
        long totalCount,
        double zeroRatio // 0 ~ 1 사이 비율
) {
}