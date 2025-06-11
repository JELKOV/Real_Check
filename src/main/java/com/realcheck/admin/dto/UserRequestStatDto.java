package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRequestStatDto
 * - 요청을 가장 많이 등록한 사용자 Top 10 조회용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestStatDto {
    // 사용자 ID
    private Long userId;
    // 사용자 닉네임
    private String nickname;
    // 작성한 요청수
    private Long requestCount;
}
