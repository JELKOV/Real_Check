package com.realcheck.point.dto;

import com.realcheck.point.entity.Point;
import com.realcheck.point.entity.PointType;
import com.realcheck.user.entity.User;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDto {
    private int amount;
    private String reason;
    private LocalDateTime earnedAt;
    private String type; // EARN, DEDUCT, REWARD 등

    /**
     * [1] DTO → Entity 변환 (포인트 지급 기록 생성)
     * - 사용자, 지급 금액, 지급 사유, 타입을 지정하여 Point 엔티티 생성
     */
    public Point toEntity(User user, PointType pointType) {
        return Point.builder()
                .user(user)
                .amount(this.amount)
                .reason(this.reason)
                .type(pointType)
                .build();
    }

    /**
     * [2] Entity → DTO 변환
     * - Point 엔티티를 DTO로 변환하여 클라이언트에 전달
     */
    public static PointDto fromEntity(Point point) {
        return new PointDto(
                point.getAmount(),
                point.getReason(),
                point.getEarnedAt(),
                point.getType() != null ? point.getType().name() : "UNKNOWN" // null-safe 처리
        );
    }
}
