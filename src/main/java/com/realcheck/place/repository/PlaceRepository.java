package com.realcheck.place.repository;

import com.realcheck.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * PlaceRepository
 * - 장소(Place) 엔티티에 대한 DB 접근을 담당하는 인터페이스
 * - Spring Data JPA를 기반으로 기본 CRUD + 사용자 정의 쿼리 제공
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

        // ─────────────────────────────────────────────
        // [1] 사용자별 장소 조회
        // ─────────────────────────────────────────────

        /**
         * [1-1] 특정 사용자가 등록한 모든 장소 목록 조회
         * - Spring Data JPA 메서드 네이밍 규칙을 따라 자동 쿼리 생성됨
         * - SQL 예시: SELECT * FROM places WHERE owner_id = ?
         *
         * @param ownerId 사용자(등록자)의 고유 ID
         * @return 사용자가 등록한 장소 목록
         */
        List<Place> findByOwnerId(Long ownerId);

        // ─────────────────────────────────────────────
        // [2] 위치 기반 검색
        // ─────────────────────────────────────────────

        /**
         * [2-1] 위도(lat), 경도(lng), 반경(m) 기준 근처 모든 장소 조회
         * - 거리 계산: MySQL의 ST_Distance_Sphere 함수 사용
         * - 거리 단위: 미터(m)
         * - SQL 예시:
         * SELECT * FROM places
         * WHERE ST_Distance_Sphere(POINT(lng, lat), POINT(:lng, :lat)) <=
         * :radiusInMeters
         *
         * @param lat            위도
         * @param lng            경도
         * @param radiusInMeters 거리 반경 (단위: 미터)
         * @return 해당 범위 내 모든 장소
         */
        @Query("""
                            SELECT p FROM Place p
                            WHERE FUNCTION('ST_Distance_Sphere', point(p.lng, p.lat), point(:lng, :lat)) <= :radiusInMeters
                        """)
        List<Place> findNearbyPlaces(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radiusInMeters") double radiusInMeters);

        /**
         * [2-2] 위도, 경도, 반경 기준 "승인된" 장소만 조회
         * - 일반 사용자에게 보여줄 때 사용 (isApproved = true)
         *
         * @param lat            위도
         * @param lng            경도
         * @param radiusInMeters 거리 반경 (단위: 미터)
         * @return 승인된 장소 리스트
         */
        @Query("""
                            SELECT p FROM Place p
                            WHERE p.isApproved = true
                            AND FUNCTION('ST_Distance_Sphere', point(p.lng, p.lat), point(:lng, :lat)) <= :radiusInMeters
                        """)
        List<Place> findApprovedNearby(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radiusInMeters") double radiusInMeters);

        // ─────────────────────────────────────────────
        // [3] 키워드 검색
        // ─────────────────────────────────────────────

        /**
         * [3-1] 장소 이름에 특정 키워드가 포함된 항목 검색 (대소문자 무시)
         * - 예: "스타벅스" → "강남스타벅스", "STARBUCKS" 등도 포함
         *
         * @param keyword 검색어
         * @return 이름에 키워드가 포함된 장소 리스트
         */
        List<Place> findByNameContainingIgnoreCase(String keyword);
}
