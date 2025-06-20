package com.realcheck.place.dto;

import java.time.LocalDateTime;

import com.realcheck.place.entity.Place;
import com.realcheck.user.entity.User;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * PlaceDto 클래스
 * - 클라이언트 ↔ 서버 간 데이터 전달용 객체
 * - Place 엔티티와 달리 DB 매핑은 없고, 필요한 필드만 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceDto {

    // 동시성 제어용 버전 필드 추가
    private Integer version;

    // ─────────────────────────────────────────────
    // [1] 기본 필드 (장소 정보)
    // ─────────────────────────────────────────────

    // 장소 ID (식별자)
    private Long id;

    // 장소 이름 (최대 100자, 필수)
    @NotBlank(message = "장소 이름은 필수입니다.")
    @Size(max = 100, message = "장소 이름은 100자 이하로 입력해주세요.")
    private String name; // 장소 이름

    // 도로명 주소 (필수)
    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    // 승인 여부 (관리자 승인 상태)
    // - true: 승인됨, false: 승인 대기 중
    private boolean approved;
    private boolean rejected;

    // 반려 이유
    private String rejectionReason;

    // 등록 및 수정 시간 - 조회 전용 (Entity → DTO 변환 시 사용)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────
    // [2] 위치 정보 (위도, 경도)
    // ─────────────────────────────────────────────

    // 위도 (필수, 범위 -90 ~ 90)
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private double lat;

    // 경도 (필수, 범위 -180 ~ 180)
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private double lng;

    // ─────────────────────────────────────────────
    // [3] 등록자 정보 (사용자 ID)
    // ─────────────────────────────────────────────

    // 등록자 ID (필수)
    @NotNull(message = "등록자(ownerId)는 필수입니다.")
    private Long ownerId;

    // ─────────────────────────────────────────────
    // [4] 변환 메서드 (DTO ↔ Entity)
    // ─────────────────────────────────────────────

    /**
     * DTO → Entity 변환
     * 클라이언트로부터 받은 DTO를 DB 저장용 Entity로 변환
     * 등록할 사용자(User) 객체를 전달받아 엔티티 생성
     */
    public Place toEntity(User owner) {
        return Place.builder()
                .name(name)
                .address(address)
                .lat(lat)
                .lng(lng)
                .owner(owner)
                .build();
    }

    /**
     * Entity → DTO 변환
     * DB에서 조회된 Place 엔티티를 클라이언트에 전달할 DTO로 변환
     */
    public static PlaceDto fromEntity(Place place) {
        return PlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .lat(place.getLat())
                .lng(place.getLng())
                .approved(place.isApproved())
                .rejected(place.isRejected())
                .rejectionReason(place.getRejectReason())
                .ownerId(place.getOwner() != null ? place.getOwner().getId() : null)
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .version(place.getVersion())

                .build();
    }
}
