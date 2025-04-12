package com.realcheck.place.dto;

import com.realcheck.place.entity.Place;
import com.realcheck.user.entity.User;

import lombok.*;

/**
 * PlaceDto 클래스
 * - 클라이언트 ↔ 서버 간 데이터 전달을 위한 객체
 * - Entity 클래스(Place)와는 다르게 DB와 직접 연결되지 않으며,
 * 필요한 정보만 추려서 API 요청/응답에 사용
 */

@Data // Getter, Setter, equals, hashCode, toString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 포함한 생성자 자동 생성
public class PlaceDto {
    private Long id; // 장소 ID
    private String name; // 장소 이름
    private String address; // 장소 주소 (ex. 도로명 주소)
    private double latitude; // 위도
    private double longitude; // 경도
    private Long ownerId; // 장소 등록자(User)의 ID

    /**
     * DTO → Entity 변환 메서드
     * 클라이언트 → DB로 저장하기 위한 변환 (DTO → Entity)
     * 
     * @param owner 등록자(User 객체)
     * @return Place 엔티티 객체
     */
    public Place toEntity(User owner) {
        Place place = new Place();
        place.setName(this.name);
        place.setAddress(this.address);
        place.setLatitude(this.latitude);
        place.setLongitude(this.longitude);
        place.setOwner(owner); // 등록자 정보 주입
        return place;
    }

    /**
     * Entity → DTO 변환 메서드
     * DB → 클라이언트에 보내기 위한 변환 (Entity → DTO)
     * 
     * @param place Place 엔티티 객체
     * @return PlaceDto로 변환된 객체
     */
    public static PlaceDto fromEntity(Place place) {
        return new PlaceDto(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getOwner() != null ? place.getOwner().getId() : null);
    }
}