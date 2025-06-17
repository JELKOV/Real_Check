package com.realcheck.point.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.realcheck.point.entity.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    // ─────────────────────────────────────────────
    // [1] 관리자 전용 - 전체 포인트 통계 조회
    // ─────────────────────────────────────────────

    /**
     * [1-1] 발행된 포인트 총량 조회
     * AdminStatsService: getTotalPointSum
     * - 관리자 대시보드 통계용
     * - CHARGE, REWARD 타입은 그대로 더하고
     * - CASH 타입은 음수로 처리해서 뺌
     * - 나머지 타입은 계산에서 제외
     */
    @Query("""
                SELECT COALESCE(SUM(
                    CASE
                        WHEN p.type IN (
                            com.realcheck.point.entity.PointType.CHARGE,
                            com.realcheck.point.entity.PointType.REWARD
                        ) THEN p.amount
                        WHEN p.type = com.realcheck.point.entity.PointType.CASH THEN -p.amount
                        ELSE 0
                    END
                ), 0)
                FROM Point p
            """)
    Long sumNetIssuedPoints();

    /**
     * [1-2] 일별 포인트 합계 조회
     * AdminPointService: getDailyAggregatedPoints
     * - 관리자 대시보드 통계용
     */
    @Query("""
              SELECT FUNCTION('DATE', p.earnedAt), SUM(p.amount)
              FROM Point p
              GROUP BY FUNCTION('DATE', p.earnedAt)
              ORDER BY FUNCTION('DATE', p.earnedAt) ASC
            """)
    List<Object[]> findDailyAggregateRaw();

    /**
     * [1-3] 최근 포인트 내역 조회 (최신 기준 내림차순)
     * AdminPointService: getRecentPointHistory
     * - 관리자 대시보드 통계용
     */
    Page<Point> findAllByOrderByEarnedAtDesc(Pageable pageable);

    // ─────────────────────────────────────────────
    // [2] 사용자 전용 - 포인트 내역 조회
    // ─────────────────────────────────────────────

    /**
     * [2-1] 특정 사용자의 포인트 내역 조회
     * PointService: getPagedPointsByUserId
     * - 마이페이지 또는 활동 내역 페이지에서 사용
     */
    Page<Point> findByUserId(Long userId, Pageable pageable);
}
