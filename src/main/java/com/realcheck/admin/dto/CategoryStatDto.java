package com.realcheck.admin.dto;

import com.realcheck.request.entity.RequestCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CategoryStatDto
 * - 카테고리별 요청 수 통계를 위한 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatDto {

    // 요청 카테고리 (예: WAITING_STATUS)
    private RequestCategory category;
    // 해당 카테고리 요청 수
    private Long count; 
}
