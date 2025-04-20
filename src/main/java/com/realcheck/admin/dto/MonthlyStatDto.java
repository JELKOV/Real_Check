package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 월별 상태 로그 등록 통계 DTO
 * - AdminStatsService → Controller에서 사용됨
 */
@Data
@AllArgsConstructor
public class MonthlyStatDto {
    private int year;     // 연도
    private int month;    // 월
    private long count;   // 등록 수
}