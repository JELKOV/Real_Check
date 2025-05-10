package com.realcheck.place.repository;

import com.realcheck.place.entity.AllowedRequestType;
import com.realcheck.request.entity.RequestCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AllowedRequestTypeRepository
 * - 허용된 요청 타입 관리 전용 Repository
 * - Place와의 관계는 유지하지만 독립적으로 관리
 */
public interface AllowedRequestTypeRepository extends JpaRepository<AllowedRequestType, Long> {

    /**
     * [1] 특정 장소에 등록된 허용된 요청 타입 목록 조회
     * - Place ID로 허용된 요청 타입들을 조회
     */
    @Query("""
            SELECT art FROM AllowedRequestType art
            WHERE art.place.id = :placeId
            """)
    List<AllowedRequestType> findByPlaceId(@Param("placeId") Long placeId);

    /**
     * [2] 특정 장소의 특정 요청 타입 삭제
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AllowedRequestType art WHERE art.place.id = :placeId AND art.requestType = :requestType")
    void deleteByPlaceIdAndRequestType(@Param("placeId") Long placeId,
            @Param("requestType") RequestCategory requestType);

    /**
     * [3] 특정 장소의 모든 요청 타입 삭제 (장소 삭제 시)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AllowedRequestType art WHERE art.place.id = :placeId")
    void deleteByPlaceId(@Param("placeId") Long placeId);

    /**
     * [4] 특정 장소에서 특정 요청 타입 존재 여부 확인
     * - 성능 최적화를 위해 DB에서 직접 확인
     */
    boolean existsByPlaceIdAndRequestType(Long placeId, RequestCategory requestType);
}
