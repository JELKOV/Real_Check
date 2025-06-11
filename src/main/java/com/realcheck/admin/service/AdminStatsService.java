package com.realcheck.admin.service;

import com.realcheck.admin.dto.CategoryLogCountDto;
import com.realcheck.admin.dto.CategoryStatDto;
import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.admin.dto.MonthlyUserStatDto;
import com.realcheck.admin.dto.TopContributingUserDto;
import com.realcheck.admin.dto.TopReportedUserDto;
import com.realcheck.admin.dto.UserRequestStatDto;
import com.realcheck.deletionlog.repository.DeletedUserLogRepository;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.request.repository.RequestRepository;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminStatsService
 * - 관리자 전용 통계 데이터를 제공하는 서비스 계층
 * - 신고 건수, 포인트 합계, 월별 로그 등록 수 등 운영 대시보드에 필요한 데이터를 처리
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final ReportRepository reportRepository;
    private final PointRepository pointRepository;
    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final RequestRepository requestRepository;
    private final DeletedUserLogRepository deletedUserLogRepository;

    // ─────────────────────────────────────────────
    // [1] 통계 - 장소 관련
    // ─────────────────────────────────────────────

    /**
     * [1-1] 상태별 장소 수 통계
     * AdminStatsController: getPlaceStatusCounts
     * - 장소의 승인 상태(승인, 반려, 펜딩)에 따른 장소 수를 집계
     * - 관리자 대시보드에서 장소 상태를 파악하는 데 사용
     */
    public Map<String, Long> getPlaceStatusCounts() {
        long approved = placeRepository.countByIsApproved(true);
        long rejected = placeRepository.countByIsRejected(true);
        long pending = placeRepository.countByIsApprovedAndIsRejected(false, false);

        return Map.of(
                "approved", approved,
                "rejected", rejected,
                "pending", pending);
    }

    /**
     * [1-2] 월별 장소 등록 수 통계
     * AdminStatsController: getMonthlyPlaceRegistrations
     * - 장소 등록 시점(createdAt)을 기준으로 연/월별 등록 수를 집계
     * - 프론트 대시보드에서 장소 등록 추세를 파악하는 데 사용
     */
    public List<MonthlyStatDto> getMonthlyPlaceRegistrations() {
        return placeRepository.countMonthlyRegistrations();
    }

    // ─────────────────────────────────────────────
    // [2] 통계 - 로그 관련
    // ─────────────────────────────────────────────

    /**
     * [2-1] 전체 신고 수 반환
     * AdminStatsController: getTotalReportCount
     * - report 테이블의 전체 레코드 수를 카운트
     * - 관리자 대시보드에서 신고 누적 현황을 파악할 때 사용
     */
    public long getTotalReportCount() {
        return reportRepository.count();
    }

    /**
     * [2-2] 카테고리별 상태로그 수 (REGISTER, ANSWER, FREE_SHARE)
     * AdminStatsController: getCategoryLogStats
     * - 카테고리별로 상태 로그의 개수를 집계하여 반환
     * - 각 카테고리의 로그 수를 시각화하여 운영 대시보드에 표시
     */
    public List<CategoryLogCountDto> getLogCountByCategory() {
        return statusLogRepository.countLogsGroupByType();
    }

    /**
     * [2-3] 월별 상태 로그 등록 수 통계 반환
     * AdminStatsController: getMonthlyStatusLogStats
     * - StatusLog 테이블의 createdAt을 기준으로 연/월별 등록 수를 집계
     * - 프론트 대시보드에 그래프나 차트용으로 활용 가능
     */
    public List<MonthlyStatDto> getMonthlyStatusLogCount() {
        return statusLogRepository.getMonthlyStatusLogCount();
    }

    // ─────────────────────────────────────────────
    // [3] 통계 - 요청 관련
    // ─────────────────────────────────────────────

    /**
     * [3-1] 월별 요청 등록 수 통계
     * AdminStatsController: getMonthlyRequestStats
     * - 요청 등록 시점(createdAt)을 기준으로 연/월별 등록 수를 집계
     * - 프론트 대시보드에서 요청 등록 추세 시각화용
     */
    public List<MonthlyStatDto> getMonthlyRequestRegistrations() {
        return requestRepository.countMonthlyRequests();
    }

    /**
     * [3-2] 카테고리별 요청 수 통계
     * AdminStatsController: getCategoryStats
     * - 요청의 카테고리별 개수를 집계하여 반환
     * - RequestCategory 기준으로 그룹화하여 개수 반환
     */
    public List<CategoryStatDto> getRequestCategoryStats() {
        return requestRepository.countByCategory();
    }

    /**
     * [3-3] 요청 상태 통계 (open / closed)
     * AdminStatsController: getRequestStatusStats
     * - 요청의 상태(열림/닫힘)에 따른 개수를 집계하여 반환
     * - isClosed 필드 기준으로 개수 집계
     */
    public Map<String, Long> getRequestStatusStats() {
        long closed = requestRepository.countByIsClosed(true);
        long open = requestRepository.countByIsClosed(false);
        return Map.of("closed", closed, "open", open);
    }

    /**
     * [3-4] 요청 Top 10 유저 통계
     * AdminStatsController: getTopUsers
     * - 요청을 가장 많이 등록한 사용자 10명을 조회
     * - 가장 많은 요청을 등록한 사용자 목록
     */
    public List<UserRequestStatDto> getTopRequestUsers() {
        return requestRepository.findTopUsersByRequestCount(PageRequest.of(0, 10));
    }

    // ─────────────────────────────────────────────
    // [4] 통계 - 유저 관련
    // ─────────────────────────────────────────────

    /**
     * [4-1] 월별 사용자 가입 통계
     * AdminStatsController: getMonthlyUserStats
     * - 사용자 가입 시점(createdAt)을 기준으로 연/월별 가입 수를 집계
     * - 프론트 대시보드에서 사용자 성장 추세를 파악하는 데 사용
     */
    public List<MonthlyUserStatDto> getMonthlyUserSignUpStats() {
        return userRepository.countMonthlySignUps();
    }

    /**
     * [4-2] 월별 탈퇴 사용자 수 반환
     * AdminStatsController: getMonthlyDeletedUsers
     * - 삭제 로그 테이블 기준
     */
    public List<MonthlyUserStatDto> getMonthlyUserDeletionStats() {
        return deletedUserLogRepository.countMonthlyDeletedUsers();
    }

    /**
     * [4-3] 신고 Top 10 유저 (가장 많이 신고당한 사용자)
     * - ReportRepository.findTopReportedUsers(PageRequest.of(0, 10)) 호출
     * - 관리자 대시보드에서 가장 많이 신고당한 사용자를 조회
     * - 신고가 많은 사용자를 파악하여 운영 정책 수립에 활용
     * - 반환 타입: List<TopReportedUserDto>
     * - 예시: 신고가 가장 많이 된 사용자 10명을 반환
     * - 반환되는 DTO는 사용자 ID, 닉네임, 신고 횟수를 포함
     */
    public List<TopReportedUserDto> getTopReportedUsers() {
        return reportRepository.findTopReportedUsers(PageRequest.of(0, 10));
    }

    /**
     * [4-4] 기여 Top 10 유저 (가장 많은 StatusLog 작성 사용자)
     * - StatusLogRepository.findTopContributors(PageRequest.of(0, 10)) 호출
     * - 관리자 대시보드에서 가장 많은 상태 로그를 작성한 사용자를 조회
     * - 기여가 많은 사용자를 파악하여 운영 정책 수립에 활용
     * - 반환 타입: List<TopContributingUserDto>
     * - 예시: 상태 로그를 가장 많이 작성한 사용자 10명을 반환
     * - 반환되는 DTO는 사용자 ID, 닉네임, 작성 횟수를 포함
     */
    public List<TopContributingUserDto> getTopContributingUsers() {
        return statusLogRepository.findTopContributors(PageRequest.of(0, 10));
    }

    /**
     * [4-5] 현재 활성/비활성 사용자 수 반환
     * AdminStatsController: getUserActiveStats
     * - 활성화된 사용자 수와 비활성화된 사용자 수를 각각 계산하여 반환
     * - 활성화된 사용자는 isActive가 true인 사용자
     * - 비활성화된 사용자는 isActive가 false인 사용자
     */
    public Map<String, Long> getUserActiveStats() {
        long activeCount = userRepository.countByIsActiveTrue();
        long inactiveCount = userRepository.countByIsActiveFalse();

        Map<String, Long> result = new HashMap<>();
        result.put("active", activeCount);
        result.put("inactive", inactiveCount);
        return result;
    }

    // ─────────────────────────────────────────────
    // [5] 통계 - 포인트 관련
    // ─────────────────────────────────────────────

    /**
     * [5-1] 전체 발행된 포인트 총량 조회
     * AdminStatsController: getTotalPointSum
     * - CHARGE, REWARD 타입은 플러스(+)
     * - CASH 타입은 마이너스(–)
     * - 그 외 타입(EARN, DEDUCT, RESERVE, REFUND 등)은 계산에서 제외
     */
    public int getTotalPointSum() {
        Long netIssued = pointRepository.sumNetIssuedPoints();
        return (netIssued != null) ? netIssued.intValue() : 0;
    }

}
