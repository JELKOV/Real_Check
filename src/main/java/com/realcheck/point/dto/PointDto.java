package com.realcheck.point.dto;

import com.realcheck.point.entity.Point;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 포인트 정보를 API 응답용으로 가공하기 위한 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDto {
    private int amount;
    private String reason;
    private LocalDateTime earnedAt;

    public static PointDto fromEntity(Point point) {
        return new PointDto(
                point.getAmount(),
                point.getReason(),
                point.getEarnedAt()
        );
    }
}
