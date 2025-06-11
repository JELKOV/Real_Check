package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopUserByPointsDto {
    // 사용자 ID
    private Long userId;
    // 사용자 닉네임
    private String nickname;
    // 실제 잔액
    private int points;
}
