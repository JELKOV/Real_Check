package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 최근 포인트 내역 테이블 행을 위한 DTO
 */
@Data
@AllArgsConstructor
public class PointHistoryDto {
    private LocalDateTime earnedAt;
    private Long userId;
    private String nickname;
    private int amount;
    private String type;
    private String reason;
}
