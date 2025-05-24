package com.realcheck.point.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.realcheck.point.entity.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    // ─────────────────────────────────────────────
    // [1] 관리자 전용 - 전체 포인트 통계 조회
    // ─────────────────────────────────────────────

    /**
     * [1-1] 전체 포인트 지급 합계 조회
     * AdminStatsService: getTotalPointSum
     * - 관리자 대시보드 통계용
     */
    @Query("SELECT SUM(p.amount) FROM Point p")
    Integer sumAllPoints();

    // ─────────────────────────────────────────────
    // [2] 사용자 전용 - 포인트 내역 조회
    // ─────────────────────────────────────────────

    /**
     * [2-1] 특정 사용자의 포인트 내역 조회 [미사용]
     * 마이페이지 또는 활동 내역 페이지에서 사용
     */
    List<Point> findByUserId(Long userId);
}
