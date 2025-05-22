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

  // ─────────────────────────────────────────────
  // [1] 기본 메소드 (CRUD 포함)
  // ─────────────────────────────────────────────

  /**
   * [1-1] 마감되지 않은 모든 요청 조회 [미사용]
   * - 예: 관리자 페이지, 전체 요청 리스트
   */
  List<Request> findByIsClosedFalse();

  /**
   * RequestService: findByUserId
   * [1-2] UserId로 사용자의 요청 목록 조회
   * 내 요청 목록 조회
   */
  List<Request> findByUserIdOrderByCreatedAtDesc(Long userId);

  /**
   * UserService: getRecentActivities
   * [1-3] UserId로 사용자의 최근 요청 5개 조회
   */
  List<Request> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

  // ─────────────────────────────────────────────
  // [2] 위치 기반 필터링 관련 메소드
  // ─────────────────────────────────────────────

  /**
   * RequestService: findOpenRequestsWithLocation
   * [2-1] 위치 기반, 카테고리 필터링된 미마감 요청 조회
   * 미마감
   * 답변 3개 미만
   * 위치(lat/lng) 존재
   * 생성 시간 기준 조회
   * 카테고리 및 반경 필터
   * 페이지네이션
   * 지도 기반 필터링 기능에서 사용됨
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
   * [2-2] 최신 요청 조회 (현재 위치 기준 / 반경 내 응답 부족 요청 조회)
   * 장소 정보가 있는 요청만 대상으로 함
   * 위도/경도 null 방지 포함
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


  // ─────────────────────────────────────────────
  // [3] 자동 마감 관련 메소드
  // ─────────────────────────────────────────────

  /**
   * RequestService: findOpenRequestsWithAnswers
   * [3-1] 자동 마감 대상 쿼리
   * 3시간 이상 지난 요청 중
   * 마감되지 않았으며
   * 숨겨지지 않은(StatusLog.isHidden = false) 답변이 1개 이상 존재하는 요청만 조회
   */
  @Query("""
          SELECT r
          FROM Request r
          WHERE r.isClosed = false
            AND r.createdAt <= :threshold
            AND EXISTS (
                SELECT 1 FROM StatusLog s
                WHERE s.request = r
                  AND s.isHidden = false
            )
      """)
  List<Request> findAllVisibleStatusLogsAfterThreshold(@Param("threshold") LocalDateTime threshold);
}
