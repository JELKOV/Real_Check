package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * [2] 월별 사용자 가입 통계 DTO
 * - User 엔티티의 createdAt 필드를 기준으로 연/월별 가입 수를 담는다.
 */
@Data
@AllArgsConstructor
public class MonthlyUserStatDto {
    // 연도 (e.g. 2025)
    private Integer year;
    // 월 (1~12)
    private Integer month;
    // 해당 연·월에 가입한 사용자 수
    private Long signUpCount;
}