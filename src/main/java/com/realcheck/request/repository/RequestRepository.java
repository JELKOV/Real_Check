package com.realcheck.request.repository;

import com.realcheck.request.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * RequestRepository 인터페이스
 * - 요청(Request) 엔티티에 대한 DB 접근을 담당하는 JPA 리포지토리
 * - 기본 CRUD 기능은 JpaRepository에서 상속
 * - 사용자 정의 쿼리를 통해 특정 조건에 맞는 요청 조회 제공
 */
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * [1] 마감되지 않은 모든 요청 조회
     * - 조건: is_closed = false
     * - 전체 열린 요청 목록을 조회할 때 사용 (관리자/리스트 등)
     *
     * SQL 예시:
     * SELECT * FROM request WHERE is_closed = false;
     *
     * @return 마감되지 않은 요청 리스트
     */
    List<Request> findByIsClosedFalse();

    /**
     * [2] 답변이 3개 미만인 열린 요청 조회
     * - 조건: is_closed = false AND statusLogs.size < 3
     * - 답변이 충분히 등록되지 않은 요청만 필터링
     * - 홈 화면 또는 "답변 대기 중 요청"에 사용
     *
     * JPQL:
     * SELECT r FROM Request r WHERE r.isClosed = false AND SIZE(r.statusLogs) < 3
     *
     * @return 미마감이며 답변 수가 3개 미만인 요청 리스트
     */
    @Query("SELECT r FROM Request r WHERE r.isClosed = false AND SIZE(r.statusLogs) < 3")
    List<Request> findOpenRequestsWithoutAnswer();

    /**
     * [3] 반경 내 답변 부족 요청 조회 (주요 API)
     * - 조건:
     *   1. 마감되지 않은 요청 (is_closed = false)
     *   2. 위도(lat), 경도(lng)가 모두 존재
     *   3. 연결된 답변(StatusLog) 개수가 3개 미만
     *   4. 사용자의 현재 위치로부터 radius(m) 이내
     *
     * - 주요 사용처: /api/request/nearby
     *   (지도에서 사용자의 위치 기준 주변 요청 조회)
     *
     * - 거리 계산 함수: ST_Distance_Sphere (MySQL의 함수 활용)
     *
     * SQL 예시:
     * SELECT * FROM request r
     * WHERE is_closed = false
     *   AND ST_Distance_Sphere(POINT(r.lng, r.lat), POINT(:lng, :lat)) <= :radius
     *   AND SIZE(r.statusLogs) < 3
     *
     * @param lat    사용자 위도
     * @param lng    사용자 경도
     * @param radius 거리 제한 (미터 단위)
     * @return 주어진 반경 내에 위치하며 답변 수가 3개 미만인 요청 리스트
     */
    @Query("""
        SELECT r FROM Request r
        WHERE r.isClosed = false
        AND r.lat IS NOT NULL AND r.lng IS NOT NULL
        AND SIZE(r.statusLogs) < 3
        AND FUNCTION('ST_Distance_Sphere', POINT(r.lng, r.lat), POINT(:lng, :lat)) <= :radius
    """)
    List<Request> findNearbyFewAnswers(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius
    );
}
