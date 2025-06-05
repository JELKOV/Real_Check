package com.realcheck.admin.entity;

/**
 * TargetType
 * - 관리자가 작업을 수행한 대상의 유형을 나타내는 열거형
 * - 로그 기록 시 대상의 종류를 명시하는 데 사용됨
 */
public enum TargetType {
    USER,       // 사용자 (회원)
    PLACE,      // 장소 (공식 장소 신청 등)
    REPORT,     // 신고 (신고 처리 관련)
    STATUS_LOG  // 상태 로그 (자발 공유, 답변 등)
}
