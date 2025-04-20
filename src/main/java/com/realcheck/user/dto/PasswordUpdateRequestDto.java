package com.realcheck.user.dto;

import lombok.Data;

/**
 * 비밀번호 변경 요청 DTO
 * - 마이페이지 등에서 현재 비밀번호 검증 후 새 비밀번호로 변경할 때 사용
 */
@Data
public class PasswordUpdateRequestDto {
    private String currentPassword;
    private String newPassword;
}
