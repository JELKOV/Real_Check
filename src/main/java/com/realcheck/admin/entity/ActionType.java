package com.realcheck.admin.entity;

/**
 * ActionType
 * - 관리자가 수행한 작업의 종류를 나타내는 열거형
 * - 로그 기록 시 어떤 행위였는지를 구분하는 데 사용됨
 */
public enum ActionType {
    BLOCK,    // 사용자/장소 등을 차단
    UNBLOCK,  // 차단 해제
    APPROVE,  // 승인 (장소 승인 등)
    REJECT    // 거절 (승인 요청 거절 등)
}
