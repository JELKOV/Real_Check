package com.realcheck.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * PlaceDetailsDto 클래스
 * - 장소 상세 정보 전달용 DTO
 * - 허용된 요청 타입 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailsDto {

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (장소 정보)
    // ─────────────────────────────────────────────

    // 장소 ID (DB 식별자)
    private Long id;
    // 장소 이름
    private String name;
    // 도로명 주소
    private String address;
    // 위도
    private double lat;
    // 경도
    private double lng;
    // 승인 여부 (공식 장소)
    private boolean isApproved;
    // ID 소유자
    private Long ownerId;

    // ─────────────────────────────────────────────
    // [2] 추가 정보 (선택적)
    // ─────────────────────────────────────────────

    // 최근 정보 (업데이트된 상태나 공지)
    private String recentInfo;
    // 커뮤니티 페이지 링크 (내부 주소로 자동 생성됨)
    private String communityLink;

    // ─────────────────────────────────────────────
    // [3] 허용된 요청 타입 (공식 장소만)
    // ─────────────────────────────────────────────

    // 허용된 요청 타입 목록 (Set<String>)
    private Set<String> allowedRequestTypes;
}
