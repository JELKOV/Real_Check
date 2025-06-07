package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * [4] 기여 Top 10 유저 DTO
 * - “가장 많은 StatusLog를 작성한” 사용자 순위 정보를 담는다.
 * - StatusLog 엔티티의 reporter(User)를 기준으로 그룹핑하여 작성 건수를 센다.
 */
@Data
@AllArgsConstructor
public class TopContributingUserDto {
    // 기여(작성) 사용자 ID (StatusLog.reporter.id)
    private Long userId;
    // 기여(작성) 사용자 닉네임
    private String nickname;
    // 해당 사용자가 작성한 StatusLog 총 건수
    private Long logCount;
}