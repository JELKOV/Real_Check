package com.realcheck.point.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.realcheck.point.entity.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    /**
     * AdminStatsService: getTotalPointSum
     * [1] 전체 포인트 지급 합계 조회
     * - 관리자 대시보드 통계용
     * - SQL:
     * SELECT SUM(amount) FROM points;
     */
    @Query("SELECT SUM(p.amount) FROM Point p")
    Integer sumAllPoints();

    // ─────────────────────────────────────────────
    // [2] 사용자 화면 또는 마이페이지에서 사용하는 기능
    // ─────────────────────────────────────────────

    /**
     * 
     * [2] 특정 사용자의 포인트 내역 조회
     * - 마이페이지 또는 활동 내역 페이지에서 사용
     * - SQL:
     * SELECT * FROM points WHERE user_id = ?;
     */
    List<Point> findByUserId(Long userId);
}
