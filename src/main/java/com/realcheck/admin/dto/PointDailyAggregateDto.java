package com.realcheck.admin.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 일별 포인트 합계 결과
 * - date: 날짜 (yyyy-MM-dd)
 * - sumAmount: 해당 일에 모든 Point.amount를 합산한 결과 (양수/음수 포함)
 */
@Data
public class PointDailyAggregateDto {

    private LocalDate date;
    private Long sumAmount;

    // (1) JPA new 연산자를 위한 생성자 (java.sql.Date → LocalDate)
    public PointDailyAggregateDto(java.sql.Date date, Long sumAmount) {
        this.date = date.toLocalDate();
        this.sumAmount = sumAmount;
    }

    // (2) 필요하다면 테스트나 수동 매핑용으로 LocalDate 생성자도 남겨둡니다
    public PointDailyAggregateDto(LocalDate date, Long sumAmount) {
        this.date = date;
        this.sumAmount = sumAmount;
    }
}
