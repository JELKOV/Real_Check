package com.realcheck.place.repository;

import com.realcheck.place.entity.FavoritePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * FavoritePlaceRepository
 * - 사용자의 즐겨찾기 장소 정보를 관리하는 JPA Repository
 * - 기본 CRUD 외에 사용자-장소 기반의 커스텀 쿼리 제공
 */
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    /**
     * [1] 특정 사용자(userId)가 특정 장소(placeId)를 즐겨찾기했는지 조회
     * PlaceService: toggleFavoritePlace
     * - 사용자가 특정 장소를 즐겨찾기할 때 호출됨
     */
    Optional<FavoritePlace> findByUserIdAndPlaceId(Long userId, Long placeId);

    /**
     * [2] 특정 사용자가 즐겨찾기한 모든 장소 조회 [미사용]
     */
    List<FavoritePlace> findByUserId(Long userId);

    /**
     * [3] 특정 사용자와 장소의 즐겨찾기 여부 확인 (존재 여부)
     * PlaceSerivice: isFavorite
     * - 사용자가 특정 장소를 즐겨찾기했는지 확인할 때 호출됨
     */
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    /**
     * [4] 특정 사용자와 장소의 즐겨찾기 정보 삭제 (즐겨찾기 해제)
     * PlaceService: toggleFavoritePlace
     * - 사용자가 특정 장소를 즐겨찾기 해제할 때 호출됨
     */
    void deleteByUserIdAndPlaceId(Long userId, Long placeId);
}
