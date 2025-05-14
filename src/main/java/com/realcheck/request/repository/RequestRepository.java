package com.realcheck.request.repository;

import com.realcheck.request.entity.Request;
import com.realcheck.request.entity.RequestCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RequestRepository
 * - Request(요청) 엔티티에 대한 데이터베이스 접근 레이어
 * - JpaRepository로 기본 CRUD 제공
 * - 사용자 정의 조건 기반 조회 쿼리 포함
 */
public interface RequestRepository extends JpaRepository<Request, Long> {

        /**
         * (미사용)
         * [1] 마감되지 않은 모든 요청 조회
         * - 예: 관리자 페이지, 전체 요청 리스트
         *
         * SQL:
         * SELECT * FROM request WHERE is_closed = false;
         */
        List<Request> findByIsClosedFalse();

        /**
         * RequestService: findOpenRequestsWithLocation
         * [2] 미마감 + 답변 3개 미만 + 위치(lat/lng) 존재 + 생성 시간 기준 조회 + 카테고리 및 반경 필터 (페이지네이션)
         * - 지도 기반 필터링 기능에서 사용됨
         * 
         * SQL:
         * SELECT r.*
         * FROM request r
         * LEFT JOIN status_log s ON r.id = s.request_id
         * WHERE r.is_closed = false
         * AND r.created_at <= ? -- 지정된 시간(threshold) 이전
         * AND r.lat IS NOT NULL
         * AND r.lng IS NOT NULL
         * AND (? IS NULL OR r.category = ?) -- 카테고리 필터 (NULL일 경우 모든 카테고리)
         * AND (
         * 6371 * acos(
         * cos(radians(?)) * cos(radians(r.lat)) *
         * cos(radians(r.lng) - radians(?)) +
         * sin(radians(?)) * sin(radians(r.lat))
         * )
         * ) <= (? / 1000) -- 반경 필터 (미터 → 킬로미터로 변환)
         * GROUP BY r.id
         * HAVING COUNT(s.id) < 3;
         */
        @Query("""
                        SELECT r
                        FROM Request r
                        WHERE r.isClosed = false
                          AND SIZE(r.statusLogs) < 3
                          AND r.createdAt <= :threshold
                          AND r.lat IS NOT NULL
                          AND r.lng IS NOT NULL
                          AND (:category IS NULL OR r.category = :category)
                          AND (6371 * acos(
                                cos(radians(:lat)) * cos(radians(r.lat)) *
                                cos(radians(r.lng) - radians(:lng)) +
                                sin(radians(:lat)) * sin(radians(r.lat))
                          )) <= (:radius / 1000)
                        """)
        Page<Request> findOpenRequestsWithLocation(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("threshold") LocalDateTime threshold,
                        @Param("category") RequestCategory category,
                        Pageable pageable);

        /**
         * RequestService: findNearbyValidRequests
         * [3] 현재 위치 기준, 반경 내 응답 부족 요청 조회
         * - 장소 정보가 있는 요청만 대상으로 함
         * - 위도/경도 null 방지 포함
         * - ST_Distance_Sphere는 MySQL 전용 함수이므로 DB 호환성 주의
         * 
         * SQL:
         * SELECT * FROM request r WHERE is_closed = false
         * AND ST_Distance_Sphere(POINT(r.lng, r.lat), POINT(:lng, :lat)) <= :radius
         * AND SIZE(r.statusLogs) < 3
         */
        @Query("""
                        SELECT r FROM Request r
                        WHERE r.isClosed = false
                                AND r.lat IS NOT NULL AND r.lng IS NOT NULL
                                AND SIZE(r.statusLogs) < 3
                                AND r.createdAt >= :timeLimit
                                AND FUNCTION('ST_Distance_Sphere', POINT(r.lng, r.lat), POINT(:lng, :lat)) <= :radius """)
        List<Request> findNearbyValidRequests(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("timeLimit") LocalDateTime timeLimit);

        /**
         * RequestService: findByUserId
         * [4] UserId 요청한사람 기준으로 요청 목록 불러오기
         */
        List<Request> findByUserIdOrderByCreatedAtDesc(Long userId);

        /**
         * RequestService: findOpenRequestsWithAnswers
         * [5] 요청 자동 마감 쿼리
         */
        @Query("""
                        SELECT r FROM Request r
                        WHERE r.isClosed = false
                          AND r.createdAt <= :threshold
                          AND SIZE(r.statusLogs) > 0
                        """)
        List<Request> findAllByIsClosedFalseAndCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
