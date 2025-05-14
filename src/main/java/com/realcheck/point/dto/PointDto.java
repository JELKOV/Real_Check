package com.realcheck.point.dto;

import com.realcheck.point.entity.Point;
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

    public static PointDto fromEntity(Point point) {
        return new PointDto(
                point.getAmount(),
                point.getReason(),
                point.getEarnedAt(),
                point.getType().name()
        );
    }
}
