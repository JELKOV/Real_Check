package com.realcheck.point.entity;

/**
 * 포인트 유형 Enum
 * - EARN: 포인트 지급
 * - DEDUCT: 포인트 차감
 */
public enum PointType {
    EARN,    // 포인트 지급
    DEDUCT,  // 포인트 차감
    REWARD   // 추가 포인트 (예: 이벤트 보상)
}
