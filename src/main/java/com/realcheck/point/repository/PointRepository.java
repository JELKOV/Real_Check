package com.realcheck.point.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.realcheck.point.entity.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    // ─────────────────────────────────────────────
    // [1] 관리자용 통계 기능
    // ─────────────────────────────────────────────

    /**
     * [1-1] 전체 포인트 지급 합계 조회
     * - 관리자 대시보드 통계용
     * - SQL 변환 예:
     * SELECT SUM(amount) FROM points;
     *
     * @return 전체 포인트 지급 합계 (null 가능성 있음 → Integer)
     */
    @Query("SELECT SUM(p.amount) FROM Point p")
    Integer sumAllPoints();

    // ─────────────────────────────────────────────
    // [2] 사용자 화면 또는 마이페이지에서 사용하는 기능
    // ─────────────────────────────────────────────

    /**
     * [2-1] 특정 사용자의 포인트 내역 조회
     * - 마이페이지 또는 활동 내역 페이지에서 사용
     * - SQL 변환 예:
     * SELECT * FROM points WHERE user_id = ?;
     *
     * @param userId 사용자 ID
     * @return 사용자의 포인트 리스트
     */
    List<Point> findByUserId(Long userId);
}
