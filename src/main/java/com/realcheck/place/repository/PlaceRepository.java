package com.realcheck.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realcheck.place.entity.Place;

/**
 * PlaceRepository
 * - 장소(Place) 엔티티와 관련된 DB 접근 로직을 처리
 * - JpaRepository<Place, Long>을 상속하면 기본적인 CRUD 기능이 자동 제공됨
 * - 필요에 따라 사용자 정의 쿼리 메서드를 추가하여 확장 가능
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // ─────────────────────────────────────────────
    // [1] 사용자 등록 장소 조회
    // ─────────────────────────────────────────────

    /**
     * [1-1] 특정 사용자가 등록한 장소들을 조회하는 메서드
     * - 메서드 이름 기반으로 Spring Data JPA가 자동 쿼리 생성
     * - SQL 변환 예:
     * SELECT * FROM places WHERE owner_id = ?
     *
     * @param ownerId 사용자(등록자)의 고유 ID
     * @return 해당 사용자가 등록한 장소 리스트
     */
    List<Place> findByOwnerId(Long ownerId);

    // ─────────────────────────────────────────────
    // [2] 위치 기반 장소 검색
    // ─────────────────────────────────────────────

    /**
     * [2-1] 반경 기반 장소 검색
     * - 주어진 위도(lat), 경도(lng)를 기준으로 일정 거리(미터 단위) 이내의 장소를 조회
     * - ST_Distance_Sphere 함수는 두 지점 간의 거리(m)를 계산하는 MySQL 함수
     *
     * SQL 변환 예:
     * SELECT * FROM places WHERE ST_Distance_Sphere(
     * POINT(longitude, latitude),
     * POINT(:lng, :lat)
     * ) <= :radiusInMeters
     *
     * @param lat            위도
     * @param lng            경도
     * @param radiusInMeters 거리(반경, 미터 단위)
     * @return 반경 내 장소 리스트
     */
    @Query("SELECT p FROM Place p WHERE " +
            "FUNCTION('ST_Distance_Sphere', " +
            "point(p.lng, p.lat), point(:lng, :lat)) <= :radiusInMeters")
    List<Place> findNearbyPlaces(@Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusInMeters") double radiusInMeters);

}
