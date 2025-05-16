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
    // 연도
    private Integer year;
    // 월   
    private Integer month;
    // 등록 수   
    private Long count;    
}