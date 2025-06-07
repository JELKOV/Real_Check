package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopUserByPointsDto {
    private Long userId;
    private String nickname;
    private int points; // 실제 잔액
}
