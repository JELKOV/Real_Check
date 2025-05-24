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
   * [1-1] 특정 사용자가 등록한 모든 장소 목록 조회 [미사용]
   * PlaceService: findByOwner
   */
  List<Place> findByOwnerId(Long ownerId);

  // ─────────────────────────────────────────────
  // [2] 위치 기반 검색
  // ─────────────────────────────────────────────

  /**
   * [2-1] 위도(lat), 경도(lng), 반경(m) 기준 근처 모든 장소 조회 [미사용]
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
   * [2-2] 위도, 경도, 반경 기준 "승인된" 장소만 조회 [미사용]
   * PlaceSerive: findApprovedNearby
   * - 일반 사용자에게 보여줄 때 사용 (isApproved = true)
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
   * [3-1] 장소 이름에 특정 키워드가 포함된 항목 검색 (대소문자 무시) [미사용]
   * - 관리자용
   * - 예: "스타벅스" → "강남스타벅스", "STARBUCKS" 등도 포함
   */
  List<Place> findByNameContainingIgnoreCase(String keyword);

  /**
   * [3-2] 승인된 장소 중 키워드 포함된 장소 검색 (대소문자 무시)
   * PlaceService: searchApprovedPlaces
   */
  @Query("""
       SELECT p FROM Place p
       WHERE p.isApproved = true
         AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
      """)
  List<Place> findApprovedByNameContaining(@Param("keyword") String keyword);
}
