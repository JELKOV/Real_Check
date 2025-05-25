package com.realcheck.status.repository;

import com.realcheck.admin.dto.MonthlyStatDto;
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
 * StatusLogRepository
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
         * [1-7] 숨김 처리된 상태 로그 조회 - 관리자용 [미사용]
         * ReportAdminService: getHiddenLogs
         */
        List<StatusLog> findByIsHiddenTrue();

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
         * [2-3] 특정 장소에 등록된 특정 타입의 상태 로그 조회 (REGISTER)
         * StatusLogService: getRegisterLogsByPlace
         * - 커뮤니티 페이지에서 공식 등록글(StatusType.REGISTER)만 조회
         * - 정렬 기준: 최신순 (createdAt 내림차순)
         */
        List<StatusLog> findByPlaceIdAndStatusTypeOrderByCreatedAtDesc(Long placeId, StatusType statusType);

        /**
         * [2-4] 특정 장소의 가장 최신 상태 로그 1건 조회 (특정 타입에 대해)
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

        // ─────────────────────────────────────────────
        // [5] 반경 필터링 조회 (사용자 위치 기반)
        // ─────────────────────────────────────────────

        /**
         * [5-1] 현재 위치 기준 반경 - 상태 로그 조회
         * StatusLogService: findNearbyStatusLogs
         * - 사용자의 위도(lat), 경도(lng)를 기반으로 일정 반경 내에 존재하는 상태 로그를 조회
         * - 숨김 여부와 관계없이 최근 생성된 로그를 대상으로 함
         */
        @Query("SELECT s FROM StatusLog s WHERE FUNCTION('ST_Distance_Sphere', POINT(s.place.lng, s.place.lat), POINT(:lng, :lat)) <= :radius AND s.createdAt >= :cutoff")
        List<StatusLog> findNearbyLogs(
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("radius") double radius,
                        @Param("cutoff") LocalDateTime cutoff);
}