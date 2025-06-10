package com.realcheck.place.repository;

import com.realcheck.place.entity.Place;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * [1-1] 특정 사용자가 등록한 모든 장소 목록 조회 [미사용]
     * PlaceService: findByOwner
     * - 추후에 장소 등록페이지구현때 적용
     */
    List<Place> findByOwnerId(Long ownerId);

    // ─────────────────────────────────────────────
    // [2] 사용자 검색
    // ─────────────────────────────────────────────

    /**
     * [2-1] 위도(lat), 경도(lng), 반경(m) 기준 근처 모든 장소 조회 - 사용자 페이지
     * PlaceSerive: findNearbyPlaces
     * - 거리 계산: MySQL의 ST_Distance_Sphere 함수 사용
     * - 거리 단위: 미터(m)
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
     * [2-2] 승인된 장소 중 키워드 포함된 장소 검색 (대소문자 무시) - 사용자 페이지
     * PlaceService: searchApprovedPlaces
     */
    @Query("""
             SELECT p FROM Place p
             WHERE p.isApproved = true
               AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Place> findApprovedByNameContaining(@Param("keyword") String keyword);

    // ─────────────────────────────────────────────
    // [3] 관리자 검색
    // ─────────────────────────────────────────────

    /**
     * [3-1] 이름 검색 페이징 (대소문자 무시) - 관리자 페이지
     * AdminPlaceService: listPlaces
     * - 이름에 특정 키워드가 포함된 장소를 페이지네이션하여 조회
     * - 대소문자 구분 없이 검색
     * - 승인 여부 무시
     */
    Page<Place> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * [3-2] 승인 여부 + 이름 검색 페이징
     * AdminPlaceService: listPlaces
     */
    Page<Place> findByIsApprovedAndNameContainingIgnoreCase(
            boolean approved, String name, Pageable pageable);

    /**
     * [3-3] 반려 여부 + 이름 검색 페이징
     * AdminPlaceService: listPlaces
     */
    Page<Place> findByIsRejectedAndNameContainingIgnoreCase(
            boolean rejected, String name, Pageable pageable);

    /**
     * [3-4] 펜딩 상태 + 이름 검색 페이징
     * AdminPlaceService: listPlaces
     */
    Page<Place> findByIsApprovedAndIsRejectedAndNameContainingIgnoreCase(
            boolean approved, boolean rejected, String name, Pageable pageable);

    /**
     * [3-5] 승인된 장소만 페이징 조회
     * AdminPlaceService: listPlaces
     */
    Page<Place> findByIsApproved(boolean approved, Pageable pageable);

    /**
     * [3-6] 반려된 장소만 페이징 조회
     * AdminPlaceService: listPlaces
     */
    Page<Place> findByIsRejected(boolean rejected, Pageable pageable);

    /**
     * [3-7] Pending 상태(승인도 반려도 아닌) 만 페이징 조회
     * AdminPlaceService: listPlaces
     */
    Page<Place> findByIsApprovedAndIsRejected(boolean approved, boolean rejected, Pageable pageable);


}
