package com.realcheck.admin.dto;

import com.realcheck.status.entity.StatusType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * [1] 카테고리별 상태로그 수 DTO
 * - StatusLog 엔티티의 statusType(enum)과 카운트 결과를 담는다.
 */
@Data
@AllArgsConstructor
public class CategoryLogCountDto {
    // StatusLog.statusType (ANSWER, FREE_SHARE, REGISTER)
    private StatusType category;
    // 해당 카테고리의 총 건수
    private Long count;
}