package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 포인트 잔액 분포 결과 (사용자 잔액 기준)
 */
@Data
@AllArgsConstructor
public class PointBalanceDistributionDto {
    private long positiveCount; // 잔액 > 0인 사용자 수
    private long zeroCount;     // 잔액 = 0인 사용자 수
}
