package com.realcheck.status.entity;

/**
 * StatusType
 * - StatusLog의 타입을 구분하기 위한 ENUM
 * - FREE_SHARE: 자유 공유 (자발적)
 * - REGISTER: 공식 장소에서 요청없는 공유
 * - ANSWER: 요청에 대한 답변
 */
public enum StatusType {
    FREE_SHARE,
    REGISTER,
    ANSWER
}