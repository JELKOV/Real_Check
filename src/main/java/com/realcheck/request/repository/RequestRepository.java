package com.realcheck.request.repository;

import com.realcheck.admin.dto.CategoryStatDto;
import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.admin.dto.UserRequestStatDto;
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
 * RequestRepository (ALL DONE)
 * - Request(요청) 엔티티에 대한 데이터베이스 접근 레이어
 * - JpaRepository로 기본 CRUD 제공
 * - 사용자 정의 조건 기반 조회 쿼리 포함
 */
public interface RequestRepository extends JpaRepository<Request, Long> {

        // ─────────────────────────────────────────────
        // [1] 기본 메소드 (CRUD 포함)
        // ─────────────────────────────────────────────

        /**
         * [1-1] UserId로 사용자의 요청 목록 조회
         * RequestService: findMyRequests
         * - 사용자 ID에 해당하는 요청만 조회
         * - 카테고리가 지정되었을 경우 해당 카테고리만 필터링
         * - 키워드가 주어지면 제목(title) 또는 내용(content)에 포함된 요청만 조회 (대소문자 무시)
         * - Pageable을 통한 정렬 및 페이지네이션 처리
         */
        @Query("""
                        SELECT r FROM Request r
                        WHERE r.user.id = :userId
                        AND (:category IS NULL OR r.category = :category)
                        AND (
                        :keyword IS NULL
                        OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        )
                        """)
        Page<Request> findMyRequestsWithFilters(
                        @Param("userId") Long userId,
                        @Param("category") RequestCategory category,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * [1-2] UserId로 사용자의 최근 요청 5개 조회
         * UserService: getRecentActivities
         */
        List<Request> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

        /**
         * [1-3] PlaceId로 요청 목록 조회
         * RequestService: getRequestsByPlaceId
         */
        List<Request> findByPlaceIdOrderByCreatedAtDesc(Long placeId);

        /**
         * [1-4] UserId로 페이지네이션된 요청 목록 조회
         * UserAdminService: getUserRequests
         */
        Page<Request> findByUserId(Long userId, Pageable pageable);

        /**
         * [1-5] 특정 장소에 등록된 요청 총수 조회
         * AdminPlaceService: getPlaceDetails
         */
        long countByPlaceId(Long placeId);

        // ─────────────────────────────────────────────
        // [2] 위치 기반 필터링 관련 메소드
        // ─────────────────────────────────────────────

        /**
         * [2-1] 위치 기반, 카테고리 필터링된 미마감 요청 조회
         * RequestService: findOpenRequestsWithLocation
         * - 미마감
         * - 답변 3개 미만
         * - 위치(lat/lng) 존재
         * - 생성 시간 기준 조회
         * - 카테고리 및 반경 필터
         * - 페이지네이션
         * - 지도 기반 필터링 기능에서 사용됨
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
         * [2-2] 최신 요청 조회 (현재 위치 기준 / 반경 내 미마감 요청 중 응답 부족 필터링, 페이지네이션 적용)
         * RequestService: findNearbyRequestsPaged
         *
         * - 장소 좌표(lat/lng)가 존재하는 요청만 대상
         * - 3시간 이내 생성된 요청만 조회
         * - 요청이 닫히지 않았으며 (isClosed = false)
         * - 숨김 처리되지 않은 응답(StatusLog)이 3개 미만인 요청만 반환
         * - ST_Distance_Sphere 함수를 이용한 반경 필터링 적용
         * - 최신순(createdAt DESC) 정렬
         */
        @Query(value = """
                        SELECT r
                        FROM Request r
                        WHERE r.isClosed = false
                          AND r.lat IS NOT NULL AND r.lng IS NOT NULL
                          AND r.createdAt >= :timeLimit
                          AND FUNCTION('ST_Distance_Sphere', POINT(r.lng, r.lat), POINT(:lng, :lat)) <= :radius
                          AND (
                              SELECT COUNT(s)
                              FROM StatusLog s
                              WHERE s.request = r
                                AND s.isHidden = false
                          ) < 3
                        """, countQuery = """
                        SELECT COUNT(r)
                        FROM Request r
                        WHERE r.isClosed = false
                          AND r.lat IS NOT NULL AND r.lng IS NOT NULL
                          AND r.createdAt >= :timeLimit
                          AND FUNCTION('ST_Distance_Sphere', POINT(r.lng, r.lat), POINT(:lng, :lat)) <= :radius
                          AND (
                              SELECT COUNT(s)
                              FROM StatusLog s
                              WHERE s.request = r
                                AND s.isHidden = false
                          ) < 3
                        """)
        Page<Request> findNearbyValidRequestsPaged(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("timeLimit") LocalDateTime timeLimit,
                        Pageable pageable);

        // ─────────────────────────────────────────────
        // [3] 자동 마감 관련 메소드
        // ─────────────────────────────────────────────

        /**
         * [3-1] 자동 마감 대상 쿼리
         * RequestService: findOpenRequestsWithAnswers
         * - 답변이 1개이상 달린 후, 3시간 이상 지난 요청 중
         * - 미마감
         * - No Hidden (응답 중 Hidden 없음)
         */

        @Query("""
                        SELECT DISTINCT r
                        FROM Request r
                        JOIN r.statusLogs s
                        WHERE r.isClosed = false
                        AND s.isHidden = false
                        AND s.createdAt <= :threshold
                        """)
        List<Request> findRequestsWithOldVisibleAnswers(@Param("threshold") LocalDateTime threshold);

        // ─────────────────────────────────────────────
        // [4] 관리자 통계 관련 메소드
        // ─────────────────────────────────────────────

        /**
         * [4-1] 월별 요청 등록 수 통계
         * AdminStatsService: getMonthlyRequestRegistrations
         * - 요청 등록 시점(createdAt)을 기준으로 연/월별 등록 수 집계
         */
        @Query("""
                        SELECT new com.realcheck.admin.dto.MonthlyStatDto(
                        YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r)
                        )
                        FROM Request r
                        GROUP BY YEAR(r.createdAt), MONTH(r.createdAt)
                        ORDER BY YEAR(r.createdAt) DESC, MONTH(r.createdAt) DESC
                        """)
        List<MonthlyStatDto> countMonthlyRequests();

        /**
         * [4-2] 카테고리별 요청 수 통계
         * AdminStatsService: getRequestCategoryStats
         * - 각 카테고리별 요청 수를 집계하여 반환
         * - RequestCategory 기준으로 그룹화
         */
        @Query("""
                        SELECT new com.realcheck.admin.dto.CategoryStatDto(
                        r.category, COUNT(r)
                        )
                        FROM Request r
                        GROUP BY r.category
                        ORDER BY COUNT(r) DESC
                        """)
        List<CategoryStatDto> countByCategory();

        /**
         * [4-3] 요청 상태 통계
         * AdminStatsService: getRequestStatusStats
         * - 요청의 상태(열림/닫힘)에 따른 개수를 집계하여 반환
         * - isClosed 필드 기반으로 요청 수 카운트
         */
        long countByIsClosed(boolean isClosed);

        /**
         * [4-4] 요청 Top 10 유저
         * AdminStatsService: getTopRequestUsers
         * - 요청을 가장 많이 등록한 사용자 10명을 조회
         * - 사용자 ID, 닉네임, 요청 수를 포함한 DTO 반환
         */
        @Query("""
                        SELECT new com.realcheck.admin.dto.UserRequestStatDto(
                        r.user.id,
                        r.user.nickname,
                        COUNT(r)
                        )
                        FROM Request r
                        GROUP BY r.user.id, r.user.nickname
                        ORDER BY COUNT(r) DESC
                        """)
        List<UserRequestStatDto> findTopUsersByRequestCount(Pageable pageable);
}
