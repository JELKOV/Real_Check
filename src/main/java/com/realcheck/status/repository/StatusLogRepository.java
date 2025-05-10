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
         * [1] 특정 장소의 상태 로그를 최신순으로 조회
         * - 관리자 페이지에서 전체 로그 보기용 (숨김 여부 무시)
         * - JPQL 기준: StatusLog s에서 조회
         * - SQL:
         * SELECT * FROM status_logs
         * WHERE place_id = ? AND created_at >= ?
         * ORDER BY created_at DESC;
         */
        @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff ORDER BY s.createdAt DESC")
        List<StatusLog> findRecentByPlaceId(@Param("placeId") Long placeId, @Param("cutoff") LocalDateTime cutoff);

        /**
         * StatusLogService: registerInternal
         * [2] 특정 사용자의 당일 등록 횟수 조회
         * - 포인트 지급 조건 체크용
         * - SQL:
         * SELECT COUNT(*) FROM status_logs
         * WHERE reporter_id = ?
         * AND created_at BETWEEN ? AND ?;
         */
        int countByReporterIdAndCreatedAtBetween(Long reporterId, LocalDateTime start, LocalDateTime end);

        /**
         * [3] 내가 작성한 모든 StatusLog 조회 (숨김 여부 관계 없음)
         * - 마이페이지나 관리자 페이지에서 사용
         * - SQL:
         * SELECT * FROM status_logs
         * WHERE reporter_id = ?
         * ORDER BY created_at DESC;
         */
        List<StatusLog> findByReporterIdOrderByCreatedAtDesc(Long userId);

        /**
         * AdminStatsService: getMonthlyStatusLogCount
         * [4] 월별 상태 로그 등록 수 통계 조회
         * - 관리자 대시보드 통계용으로 사용
         * - 연도 및 월별로 그룹화하여 등록된 StatusLog 개수를 반환
         * 
         * SQL:
         * SELECT YEAR(created_at) AS year,
         * MONTH(created_at) AS month,
         * COUNT(*) AS count
         * FROM status_logs
         * GROUP BY YEAR(created_at), MONTH(created_at)
         * ORDER BY YEAR(created_at), MONTH(created_at);
         */
        @Query("SELECT new com.realcheck.admin.dto.MonthlyStatDto(YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s)) " +
                        "FROM StatusLog s GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)")
        List<MonthlyStatDto> getMonthlyStatusLogCount();

        // ─────────────────────────────────────────────
        // [2] 사용자 화면에 노출할 용도 (isHidden = false 필터링 포함)
        // ─────────────────────────────────────────────

        /**
         * [1] 특정 장소에 대해 3시간 이내 + 숨김 처리되지 않은 로그만 조회
         * - 사용자 화면에 표시할 로그 필터링용
         * - SQL:
         * SELECT * FROM status_logs
         * WHERE place_id = ?
         * AND created_at >= ?
         * AND is_hidden = false
         * ORDER BY created_at DESC;
         */
        @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff AND s.isHidden = false ORDER BY s.createdAt DESC")
        List<StatusLog> findVisibleRecentByPlaceId(@Param("placeId") Long placeId,
                        @Param("cutoff") LocalDateTime cutoff);

        /**
         * [2] 내가 등록한 상태 로그 중 숨김 처리되지 않은 로그만 조회
         * - 마이페이지에서 공개 게시물만 표시
         * - SQL:
         * SELECT * FROM status_logs
         * WHERE reporter_id = ?
         * AND is_hidden = false
         * ORDER BY created_at DESC;
         */
        List<StatusLog> findByReporterIdAndIsHiddenFalseOrderByCreatedAtDesc(Long userId);

        /**
         * [3] 특정 장소의 가장 최신 상태 로그 1개 조회
         * - 사용자 화면에서 마커 클릭 시 최신 상태 표시용
         * - 숨김 처리되지 않은 최신 로그 1개만 반환
         * - SQL:
         * SELECT * FROM status_logs
         * WHERE place_id = ?
         * AND is_hidden = false
         * ORDER BY created_at DESC
         * LIMIT 1;
         */
        StatusLog findTopByPlaceIdAndIsHiddenFalseOrderByCreatedAtDesc(Long placeId);

        /**
         * StatusLogService: registerAnswer
         * [4] 특정 요청(Request)에 등록된 답변(StatusLog)의 개수 조회 
         * - 요청당 최대 3개의 답변만 허용하기 위한 제약 조건 검사 시 사용
         * - SQL:
         * SELECT COUNT(*) FROM status_logs
         * WHERE request_id = ?;
         */
        long countByRequestId(Long requestId);

        /**
         * StatusLogService: getAnswersByRequestId
         * [5] 특정 요청(Request)에 연결된 모든 상태 로그 조회 (상세 조회) 
         * - 요청 상세 페이지에서 답변(StatusLog) 목록 표시용
         * - SQL:
         * SELECT * FROM status_logs
         * WHERE request_id = ?;
         */
        List<StatusLog> findByRequestId(Long requestId);

        /**
         * StatusLogService: findNearbyStatusLogs
         * [6] 현재 위치 기준 반경 - 상태 로그 조회 
         * - 사용자의 위도(lat), 경도(lng)를 기반으로 일정 반경 내에 존재하는 상태 로그를 조회
         * - 숨김 여부와 관계없이 최근 생성된 로그를 대상으로 함
         * 
         * SQL:
         * SELECT * FROM status_logs
         * WHERE ST_Distance_Sphere(POINT(place.lng, place.lat), POINT(:lng, :lat)) <=
         * :radius
         * AND created_at >= :cutoff;
         */
        @Query("SELECT s FROM StatusLog s WHERE FUNCTION('ST_Distance_Sphere', POINT(s.place.lng, s.place.lat), POINT(:lng, :lat)) <= :radius AND s.createdAt >= :cutoff")
        List<StatusLog> findNearbyLogs(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("cutoff") LocalDateTime cutoff);
}