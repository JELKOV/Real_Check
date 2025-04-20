package com.realcheck.status.repository;

import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.status.entity.StatusLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * StatusLogRepository
 * - DB와 통신하는 계층 (JPA Repository)
 * - 기본 CRUD + 장소별/사용자별 조건 조회 기능 제공
 */
public interface StatusLogRepository extends JpaRepository<StatusLog, Long> {

    // ─────────────────────────────────────────────
    // [1] 공통 사용 또는 관리자용
    // ─────────────────────────────────────────────

    /**
     * [1-1] 특정 장소의 상태 로그를 최신순으로 조회
     * - 관리자 페이지에서 전체 로그 보기용 (숨김 여부 무시)
     * - JPQL 기준: StatusLog s에서 조회
     * - SQL 변환 예:
     * SELECT * FROM status_logs
     * WHERE place_id = ? AND created_at >= ?
     * ORDER BY created_at DESC;
     */
    @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff ORDER BY s.createdAt DESC")
    List<StatusLog> findRecentByPlaceId(@Param("placeId") Long placeId, @Param("cutoff") LocalDateTime cutoff);

    /**
     * [1-2] 특정 사용자의 당일 등록 횟수 조회
     * - 포인트 지급 조건 체크용
     * - SQL 변환 예:
     * SELECT COUNT(*) FROM status_logs
     * WHERE reporter_id = ?
     * AND created_at BETWEEN ? AND ?;
     */
    int countByReporterIdAndCreatedAtBetween(Long reporterId, LocalDateTime start, LocalDateTime end);

    /**
     * [1-3] 내가 작성한 모든 StatusLog 조회 (숨김 여부 관계 없음)
     * - 마이페이지나 관리자 페이지에서 사용
     * - SQL 변환 예:
     * SELECT * FROM status_logs
     * WHERE reporter_id = ?
     * ORDER BY created_at DESC;
     */
    List<StatusLog> findByReporterIdOrderByCreatedAtDesc(Long userId);

    /**
     * [1-4] 월별 상태 로그 등록 수 통계 조회
     * - 관리자 대시보드 통계용으로 사용
     * - 연도 및 월별로 그룹화하여 등록된 StatusLog 개수를 반환
     * 
     * SQL 변환 예:
     * SELECT YEAR(created_at) AS year,
     * MONTH(created_at) AS month,
     * COUNT(*) AS count
     * FROM status_logs
     * GROUP BY YEAR(created_at), MONTH(created_at)
     * ORDER BY YEAR(created_at), MONTH(created_at);
     *
     * @return 월별 등록 통계 DTO 리스트 (MonthlyStatDto)
     */
    @Query("SELECT new com.realcheck.admin.dto.MonthlyStatDto(YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s)) " +
            "FROM StatusLog s GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)")
    List<MonthlyStatDto> getMonthlyStatusLogCount();

    // ─────────────────────────────────────────────
    // [2] 사용자 화면에 노출할 용도 (isHidden = false 필터링 포함)
    // ─────────────────────────────────────────────

    /**
     * [2-1] 특정 장소에 대해 3시간 이내 + 숨김 처리되지 않은 로그만 조회
     * - 사용자 화면에 표시할 로그 필터링용
     * - SQL 변환 예:
     * SELECT * FROM status_logs
     * WHERE place_id = ?
     * AND created_at >= ?
     * AND is_hidden = false
     * ORDER BY created_at DESC;
     */
    @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff AND s.isHidden = false ORDER BY s.createdAt DESC")
    List<StatusLog> findVisibleRecentByPlaceId(@Param("placeId") Long placeId, @Param("cutoff") LocalDateTime cutoff);

    /**
     * [2-2] 내가 등록한 상태 로그 중 숨김 처리되지 않은 로그만 조회
     * - 마이페이지에서 공개 게시물만 표시
     * - SQL 변환 예:
     * SELECT * FROM status_logs
     * WHERE reporter_id = ?
     * AND is_hidden = false
     * ORDER BY created_at DESC;
     */
    List<StatusLog> findByReporterIdAndIsHiddenFalseOrderByCreatedAtDesc(Long userId);

}