package com.realcheck.status.repository;

import com.realcheck.admin.dto.CategoryLogCountDto;
import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.admin.dto.TopContributingUserDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * StatusLogRepository (ALL DONE)
 * - DB와 통신하는 계층 (JPA Repository)
 * - 기본 CRUD + 장소별/사용자별 조건 조회 기능 제공
 */
public interface StatusLogRepository extends JpaRepository<StatusLog, Long> {

        // ─────────────────────────────────────────────
        // [1] 사용자 기반 조건 조회
        // ─────────────────────────────────────────────

        /**
         * [1-1] 내가 작성한 모든 StatusLog 조회 (숨김 여부 관계 없음)
         * StatusLogService: getLogsByUser
         * - 마이페이지 기본 조회용
         * - 정렬 포함 (Pageable의 Sort)
         */
        Page<StatusLog> findByReporter_Id(Long userId, Pageable pageable);

        /**
         * [1-2] 특정 타입(StatusType)의 로그만 조회
         * StatusLogService: getLogsByUser
         * - ex) 자발 공유만, 요청 답변만
         */
        Page<StatusLog> findByReporter_IdAndStatusType(Long userId, StatusType type, Pageable pageable);

        /**
         * [1-3] 숨김처리 되지 않은 로그만 조회
         * StatusLogService: getLogsByUser
         * - "신고된 답변 제외" 필터 적용 시 사용
         */
        Page<StatusLog> findByReporter_IdAndIsHiddenFalse(Long userId, Pageable pageable);

        /**
         * [1-4] 특정 타입 + 숨김 제외 조건을 모두 만족하는 로그 조회
         * StatusLogService: getLogsByUser
         * - 자발공유 중 신고되지 않은 것만
         */
        Page<StatusLog> findByReporter_IdAndStatusTypeAndIsHiddenFalse(Long userId, StatusType type, Pageable pageable);

        /**
         * [1-5] 특정 사용자의 당일 등록 횟수 조회
         * StatusLogService: registerInternal
         */
        int countByReporterIdAndCreatedAtBetween(Long reporterId, LocalDateTime start, LocalDateTime end);

        /**
         * [1-6] 특정 사용자의 최근 답변 5개 조회
         * UserService: getRecentActivities
         */
        List<StatusLog> findTop5ByReporterIdOrderByCreatedAtDesc(Long userId);

        /**
         * [1-7] 특정 사용자가 작성한 모든 상태 로그 조회
         * UserAdminService: getUserStatusLogs
         * - 관리자 페이지에서 특정 사용자의 모든 상태 로그를 조회할 때 사용
         */
        Page<StatusLog> findByReporterId(Long userId, Pageable pageable);

        /**
         * [1-8] 특정 사용자가 작성한 상태 로그 중 지정된 타입(StatusType)만 필터링 조회
         * UserAdminService: getUserStatusLogs
         * - 관리자 페이지에서 상태 로그를 유형별로 필터링할 때 사용 (예: ANSWER, FREE_SHARE 등)
         */
        Page<StatusLog> findByReporterIdAndStatusType(Long userId, StatusType statusType, Pageable pageable);

        // ─────────────────────────────────────────────
        // [2] 장소 기반 조건 조회
        // ─────────────────────────────────────────────

        /**
         * [2-1] 특정 장소의 상태 로그를 최신순으로 조회
         * StatusLogService: getLogsByPlace
         * - 전체 로그 보기용 (숨김 여부 무시)
         */
        @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.createdAt >= :cutoff ORDER BY s.createdAt DESC")
        List<StatusLog> findRecentByPlaceId(@Param("placeId") Long placeId, @Param("cutoff") LocalDateTime cutoff);

        /**
         * [2-2] 특정 장소의 가장 최신 공지(REGISTER) 로그 1개 조회
         * StatusLogService: getLatestRegisterLogByPlaceId
         * - 사용자 화면에서 마커에서 보여줌
         * - 숨김 처리되지 않은 최신 로그 1개만 반환
         */
        @Query("SELECT s FROM StatusLog s WHERE s.place.id = :placeId AND s.statusType = 'REGISTER' AND s.isHidden = false ORDER BY s.createdAt DESC")
        List<StatusLog> findVisibleRegisterLogsByPlace(@Param("placeId") Long placeId);

        /**
         * [2-3] 특정 장소의 REGISTER 타입 상태 로그를 페이지 단위로 조회
         * StatusLogService: getPagedRegisterLogsByPlace
         * - 공식 공지글 리스트를 프론트에서 페이지네이션 처리 가능하도록 제공
         * - 정렬 기준: createdAt 내림차순
         */
        Page<StatusLog> findByPlaceIdAndStatusType(Long placeId, StatusType statusType, Pageable pageable);

        /**
         * [2-4] 특정 장소의 자발적 공유 상태 로그(FREE_SHARE) 조회
         * StatusLogService: findNearbyFreeShareLogs
         * - 장소 주변의 자발적 공유 상태 로그를 조회
         * - 사용자의 위치(lat, lng)와 반경(radius)을 기준으로 조회
         * - 숨김 처리되지 않은 로그만 반환
         */
        @Query("""
                            SELECT s FROM StatusLog s
                            WHERE s.statusType = 'FREE_SHARE'
                              AND s.lat IS NOT NULL AND s.lng IS NOT NULL
                              AND s.isHidden = false
                              AND s.createdAt >= :cutoff
                              AND function('ST_Distance_Sphere', point(:lng, :lat), point(s.lng, s.lat)) <= :radius
                        """)
        Page<StatusLog> findNearbyFreeShareLogs(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("cutoff") LocalDateTime cutoff,
                        Pageable pageable);

        /**
         * [2-5] 특정 장소의 가장 최신 상태 로그 1건 조회 (특정 타입에 대해)
         * PlaceService: getPlaceDetails
         * - 사용 예시: 장소 상세 정보 recentInfo에 최신 REGISTER 로그 내용 표시 시 활용
         * - StatusType.REGISTER에 제한하지 않고 다양한 타입 사용 가능
         * - 결과는 createdAt 기준 가장 최신 1건
         */
        StatusLog findTopByPlaceIdAndStatusTypeOrderByCreatedAtDesc(Long placeId, StatusType statusType);

        // ─────────────────────────────────────────────
        // [3] 요청(Request) 기반 조건 조회
        // ─────────────────────────────────────────────

        /**
         * [3-1] 특정 요청(Request)에 연결된 모든 상태 로그 조회 (상세 조회)
         * StatusLogService: getAnswersByRequestId
         * - 요청 상세 페이지에서 답변(StatusLog) 목록 표시용
         */
        List<StatusLog> findByRequestId(Long requestId);

        /**
         * [3-2] 특정 요청(Request)에 등록된 답변(StatusLog)의 개수 조회
         * StatusLogService: registerAnswer
         * RequestSerivce: countVisibleStatusLogsByRequestId
         * - 요청당 최대 3개의 답변만 허용하기 위한 제약 조건 검사 시 사용
         */
        long countByRequestIdAndIsHiddenFalse(Long requestId);

        /**
         * [3-3] 특정 요청(Request)에 동일 사용자가 이미 답변을 등록했는지 확인
         * StatusLogRepository: registerAnswer
         * - 중복 답변 방지를 위한 제약 조건 검사에 사용
         */
        @Query("SELECT COUNT(s) > 0 FROM StatusLog s WHERE s.request.id = :requestId AND s.reporter.id = :userId")
        boolean existsByRequestIdAndUserId(@Param("requestId") Long requestId, @Param("userId") Long userId);

        // ─────────────────────────────────────────────
        // [4] 통계/관리자용 조회
        // ─────────────────────────────────────────────

        /**
         * [4-1] 월별 상태 로그 등록 수 통계 조회
         * AdminStatsService: getMonthlyStatusLogCount
         * - 관리자 대시보드 통계용으로 사용
         * - 연도 및 월별로 그룹화하여 등록된 StatusLog 개수를 반환
         */
        @Query("SELECT new com.realcheck.admin.dto.MonthlyStatDto(YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s)) " +
                        "FROM StatusLog s GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)")
        List<MonthlyStatDto> getMonthlyStatusLogCount();

        /**
         * [4-2] 카테고리별 상태로그 수 집계
         * AdminStatsService: getLogCountByCategory
         * - 각 카테고리(REGISTER, ANSWER, FREE_SHARE 등)별로 상태 로그의 개수를 집계하여 반환
         */
        @Query("""
                                SELECT new com.realcheck.admin.dto.CategoryLogCountDto(
                                        sl.statusType,
                                        COUNT(sl)
                                        )
                                FROM StatusLog sl
                                GROUP BY sl.statusType
                        """)
        List<CategoryLogCountDto> countLogsGroupByType();

        /**
         * [4-3] 공헌 많은 사용자 조회 Top 10
         * AdminStatsService: getTopContributingUsers
         * - 관리자 대시보드에서 가장 많은 상태 로그를 등록한 사용자를 조회
         * - 사용자 ID, 닉네임, 등록한 상태 로그 개수를 포함하는 DTO 반환
         */
        @Query("""
                                SELECT new com.realcheck.admin.dto.TopContributingUserDto(
                                        sl.reporter.id,
                                        sl.reporter.nickname,
                                        COUNT(sl)
                                        )
                                FROM StatusLog sl
                                GROUP BY sl.reporter.id, sl.reporter.nickname
                                ORDER BY COUNT(sl) DESC
                        """)
        List<TopContributingUserDto> findTopContributors(Pageable pageable);

        /**
         * [4-4] 특정 장소에 등록된 상태 로그(ANSWER/REGISTER 등) 총수 조회
         * AdminPlaceService: getPlaceDetails
         */
        long countByPlaceId(Long placeId);

        /**
         * [4-5] 특정 사용자가 작성한 상태 로그의 총 개수 조회 - 관리자용
         * StatusLogService: countLogsByUserId
         * - 관리자 페이지에서 사용자별 상태 로그 개수 통계용
         * - 숨김 여부와 관계없이 전체 로그 개수 반환
         */
        long countByReporter_Id(Long userId);

        /**
         * [4-6] 숨김 처리된 상태 로그 조회 - 관리자용
         * ReportAdminService: getHiddenLogs
         * - 관리자 페이지에서 숨김 처리된 상태 로그를 조회할 때 사용
         * - 숨김 처리된 로그만 반환
         */
        List<StatusLog> findByIsHiddenTrue();

        /**
         * [4-7] FREE_SHARE 로그만 필터링 - 관리자용
         * StatusLogAdminService: getFreeShareLogs
         */
        Page<StatusLog> findByStatusType(StatusType type, Pageable pageable);

        // ─────────────────────────────────────────────
        // [5] 반경 필터링 조회 (사용자 위치 기반)
        // ─────────────────────────────────────────────

        /**
         * [5-1] 현재 위치 기준 반경 - 상태 로그 조회
         * StatusLogService: findNearbyGroupedPlaceLogs
         */
        @Query("""
                            SELECT s
                            FROM StatusLog s
                            WHERE s.statusType IN (com.realcheck.status.entity.StatusType.ANSWER, com.realcheck.status.entity.StatusType.REGISTER)
                              AND FUNCTION('ST_Distance_Sphere', POINT(s.lng, s.lat), POINT(:lng, :lat)) <= :radius
                              AND s.createdAt >= :cutoff
                              AND s.isHidden = false
                        """)
        List<StatusLog> findNearbyAnswerAndRegisterLogs(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("cutoff") LocalDateTime cutoff);

        /**
         * [5-2] 일반 장소 위치의 ANSWER 로그만 필터링
         * StatusLogService: findNearbyUserLocationLogs
         */
        @Query("""
                            SELECT s FROM StatusLog s
                            WHERE s.statusType = com.realcheck.status.entity.StatusType.ANSWER
                              AND s.place IS NULL
                              AND FUNCTION('ST_Distance_Sphere', POINT(s.lng, s.lat), POINT(:lng, :lat)) <= :radius
                              AND s.createdAt >= :cutoff
                              AND s.isHidden = false
                        """)
        Page<StatusLog> findNearbyUserAnswerLogs(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("cutoff") LocalDateTime cutoff,
                        Pageable pageable);
}