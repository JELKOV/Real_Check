package com.realcheck.admin.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPlaceDetailsDto {
    /** 장소 식별자 */
    private Long id;
    /** 이름 */
    private String name;
    /** 주소 */
    private String address;
    /** 위도 */
    private double lat;
    /** 경도 */
    private double lng;
    /** 승인 여부 */
    private boolean approved;
    private boolean rejected;

    public boolean isPending() {
        return !approved && !rejected;
    }

    /** 등록자 ID */
    private Long ownerId;
    /** 등록자 이름 */
    private String ownerName;
    /** 생성 시각 */
    private LocalDateTime createdAt;
    /** 수정 시각 */
    private LocalDateTime updatedAt;

    /** 공식 장소일 경우 허용된 요청 타입 리스트 */
    private Set<String> allowedRequestTypes;
    /** 가장 최근 REGISTER(공지) 로그의 내용 */
    private String recentInfo;
    /** 커뮤니티 페이지로 이동 가능한 내부 링크 */
    private String communityLink;

    /** (옵션) 즐겨찾기 누적 수 */
    private long favoriteCount;
    /** (옵션) 해당 장소에 등록된 요청 총수 */
    private long requestCount;
    /** (옵션) 해당 장소에 등록된 상태 로그(ANSWER/REGISTER) 총수 */
    private long statusLogCount;

    /**
     * 서비스에서 채워넣을 때 편하게 쓰는 변환 메서드
     */
    public static AdminPlaceDetailsDto of(
            com.realcheck.place.entity.Place place,
            String ownerName,
            Set<String> allowedTypes,
            String recentInfo,
            String communityLink,
            long favoriteCount,
            long requestCount,
            long statusLogCount) {
        return AdminPlaceDetailsDto.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .lat(place.getLat())
                .lng(place.getLng())
                .approved(place.isApproved())
                .rejected(place.isRejected())
                .ownerId(place.getOwner() != null ? place.getOwner().getId() : null)
                .ownerName(ownerName)
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .allowedRequestTypes(allowedTypes)
                .recentInfo(recentInfo)
                .communityLink(communityLink)
                .favoriteCount(favoriteCount)
                .requestCount(requestCount)
                .statusLogCount(statusLogCount)
                .build();
    }
}
