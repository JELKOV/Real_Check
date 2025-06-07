package com.realcheck.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * [3] 신고 Top 10 유저 DTO
 * - “가장 많이 신고당한” 사용자 순위 정보를 담는다.
 * - Report 엔티티를 조회할 때, Report → StatusLog → StatusLog.reporter(User)로 연결되어
 *   최종적으로 User.id와 User.nickname을 꺼내야 한다.
 */
@Data
@AllArgsConstructor
public class TopReportedUserDto {
    // 신고당한 사용자 ID (StatusLog.reporter.id)
    private Long userId;
    // 신고당한 사용자 닉네임
    private String nickname;
    // 해당 사용자가 신고당한 총 건수
    private Long reportCount;
}