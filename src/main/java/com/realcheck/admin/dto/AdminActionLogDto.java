package com.realcheck.admin.dto;

import java.time.LocalDateTime;

import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.AdminActionLog;
import com.realcheck.admin.entity.TargetType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AdminActionLogDto
 * - 관리자 행동 로그를 클라이언트에 전달하기 위한 DTO
 * - 민감한 정보는 포함하지 않음 (예: 비밀번호, 이메일 등)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionLogDto {

    private Long id;
    private Long adminId;
    private String adminNickname;
    private ActionType actionType;
    private TargetType targetType;
    private Long targetId;
    private String description;
    private LocalDateTime createdAt;

    /**
     * AdminActionLog → AdminActionLogDto 변환
     */
    public static AdminActionLogDto fromEntity(AdminActionLog log) {
        AdminActionLogDto dto = new AdminActionLogDto();
        dto.setId(log.getId());
        dto.setAdminId(log.getAdminId());
        dto.setAdminNickname(
                log.getAdmin() != null ? log.getAdmin().getNickname() : null);
        dto.setActionType(log.getActionType());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setDescription(log.getDescription());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
