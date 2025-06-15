package com.realcheck.point.entity;

/**
 * 포인트 유형 Enum
 * - EARN: 답변 채택, 자동 분배 등으로 지급된 포인트 (요청/답변)
 * - DEDUCT: 기능 사용 등으로 차감된 포인트 (요청/답변)
 * - REWARD: 관리자나 이벤트 등으로 지급된 포인트 (자발적 공유 포함)
 * - RESERVE: 요청 등록 시 예치된 포인트
 * - REFUND: 예치 후 환불된 포인트
 * - CHARGE: 사용자가 직접 충전한 포인트
 * - CASH: 사용자가 현금화(환전)한 포인트
 */
public enum PointType {
    EARN,
    DEDUCT,
    REWARD,
    RESERVE,
    REFUND,
    CHARGE,
    CASH
}