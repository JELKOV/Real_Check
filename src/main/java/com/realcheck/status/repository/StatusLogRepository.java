package com.realcheck.status.repository;

import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.status.entity.StatusLog;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * StatusLogRepository
 * - DB와 통신하는 계층 (JPA Repository)
 * - 기본 CRUD + 장소별/사용자별 조건 조회 기능 제공
 */
public interface StatusLogRepository extends JpaRepository<StatusLog, Long> {

        // ─────────────────────────────────────────────
        // [1] 사용자 기반 조건 조회
        // ─────────────────────────────────────────────

        /**
         * StatusLogService: getLogsByUser
         * [1-1] 내가 작성한 모든 StatusLog 조회 (숨김 여부 관계 없음)
         * 마이페이지나 관리자 페이지에서 사용
         */
        List<StatusLog> findByReporterIdOrderByCreatedAtDesc(Long userId);

        /**
         * StatusLogService: registerInternal
         * [1-2] 특정 사용자의 당일 등록 횟수 조회
         */
        int countByReporterIdAndCreatedAtBetween(Long reporterId, LocalDateTime start, LocalDateTime end);

        /**
         * UserService: getRecentActivities
         * [1-3] 특정 사용자의 최근 답변 5개 조회
         */
        List<StatusLog> findTop5ByReporterIdOrderByCreatedAtDesc(Long userId);

        /**
         * [1-4] 내가 등록한 상태 로그 중 숨김 처리되지 않은 로그만 조회 [미사용]
         * 마이 로그 페이지에서 공개 게시물만 표시
         */
        List<StatusLog> findByReporterIdAndIsHiddenFalseOrderByCreatedAtDesc(Long userId);

        /**
         * UserService: deleteUserAndRelatedData
         * [1-5] 사용자의 답변 전체 삭제 (탈퇴 시 사용)
         */
        @Modifying
        @Query("DELETE FROM StatusLog s WHERE s.reporter.id = :userId")
        void deleteByReporterId(@Param("userId") Long userId);

        // ─────────────────────────────────────────────
        // [2] 장소 기반 조건 조회
        // ─────────────────────────────────────────────

        /**
         * StatusLogService: getLogsByPlace
         * [2-1] 특정 장소의 상태 로그를 최신순으로 조회 [미사용]
         * 관리자 페이지에서 전체 로그 보기용 (숨김 여부 무시)
         */
        @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff ORDER BY s.createdAt DESC")
        List<StatusLog> findRecentByPlaceId(@Param("placeId") Long placeId, @Param("cutoff") LocalDateTime cutoff);

        /**
         * [2-2] 특정 장소에 대해 3시간 이내 + 숨김 처리되지 않은 로그만 조회 [미사용]
         * 사용자 화면에 표시할 로그 필터링용
         */
        @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff AND s.isHidden = false ORDER BY s.createdAt DESC")
        List<StatusLog> findVisibleRecentByPlaceId(@Param("placeId") Long placeId,
                        @Param("cutoff") LocalDateTime cutoff);

        /**
         * [2-3] 특정 장소의 가장 최신 상태 로그 1개 조회 [미사용]
         * 사용자 화면에서 마커 클릭 시 최신 상태 표시용
         * 숨김 처리되지 않은 최신 로그 1개만 반환
         */
        StatusLog findTopByPlaceIdAndIsHiddenFalseOrderByCreatedAtDesc(Long placeId);

        // ─────────────────────────────────────────────
        // [3] 요청(Request) 기반 조건 조회
        // ─────────────────────────────────────────────

        /**
         * StatusLogService: getAnswersByRequestId
         * [3-1] 특정 요청(Request)에 연결된 모든 상태 로그 조회 (상세 조회)
         * 요청 상세 페이지에서 답변(StatusLog) 목록 표시용
         */
        List<StatusLog> findByRequestId(Long requestId);

        /**
         * StatusLogService: registerAnswer
         * [3-2] 특정 요청(Request)에 등록된 답변(StatusLog)의 개수 조회
         * 요청당 최대 3개의 답변만 허용하기 위한 제약 조건 검사 시 사용
         */
        long countByRequestId(Long requestId);

        /**
         * StatusLogRepository: registerAnswer
         * [3-3] 특정 요청(Request)에 동일 사용자가 이미 답변을 등록했는지 확인
         * 중복 답변 방지를 위한 제약 조건 검사에 사용
         */
        @Query("SELECT COUNT(s) > 0 FROM StatusLog s WHERE s.request.id = :requestId AND s.reporter.id = :userId")
        boolean existsByRequestIdAndUserId(@Param("requestId") Long requestId, @Param("userId") Long userId);

        // ─────────────────────────────────────────────
        // [4] 통계/관리자용 조회
        // ─────────────────────────────────────────────

        /**
         * AdminStatsService: getMonthlyStatusLogCount
         * [4-1] 월별 상태 로그 등록 수 통계 조회
         * 관리자 대시보드 통계용으로 사용
         * 연도 및 월별로 그룹화하여 등록된 StatusLog 개수를 반환
         */
        @Query("SELECT new com.realcheck.admin.dto.MonthlyStatDto(YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s)) " +
                        "FROM StatusLog s GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)")
        List<MonthlyStatDto> getMonthlyStatusLogCount();

        // ─────────────────────────────────────────────
        // [5] 반경 필터링 조회 (사용자 위치 기반)
        // ─────────────────────────────────────────────

        /**
         * StatusLogService: findNearbyStatusLogs
         * [5-1] 현재 위치 기준 반경 - 상태 로그 조회
         * 사용자의 위도(lat), 경도(lng)를 기반으로 일정 반경 내에 존재하는 상태 로그를 조회
         * 숨김 여부와 관계없이 최근 생성된 로그를 대상으로 함
         */
        @Query("SELECT s FROM StatusLog s WHERE FUNCTION('ST_Distance_Sphere', POINT(s.place.lng, s.place.lat), POINT(:lng, :lat)) <= :radius AND s.createdAt >= :cutoff")
        List<StatusLog> findNearbyLogs(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("cutoff") LocalDateTime cutoff);
}