package com.realcheck.admin.service;

import com.realcheck.admin.dto.CategoryLogCountDto;
import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.admin.dto.MonthlyUserStatDto;
import com.realcheck.admin.dto.TopContributingUserDto;
import com.realcheck.admin.dto.TopReportedUserDto;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // ─────────────────────────────────────────────
    // [1] 통계 메서드 (신고, 포인트, 월별 로그)
    // ─────────────────────────────────────────────

    /**
     * [1-1] 전체 신고 수 반환
     * AdminStatsController: getTotalReportCount
     * - report 테이블의 전체 레코드 수를 카운트
     * - 관리자 대시보드에서 신고 누적 현황을 파악할 때 사용
     */
    public long getTotalReportCount() {
        return reportRepository.count();
    }

    /**
     * [1-2] 전체 발행된 포인트 총량 조회
     * - CHARGE, REWARD 타입은 플러스(+)
     * - CASH 타입은 마이너스(–)
     * - 그 외 타입(EARN, DEDUCT, RESERVE, REFUND 등)은 계산에서 제외
     */
    public int getTotalPointSum() {
        Long netIssued = pointRepository.sumNetIssuedPoints();
        return (netIssued != null) ? netIssued.intValue() : 0;
    }

    /**
     * [1-3] 월별 상태 로그 등록 수 통계 반환
     * AdminStatsController: getMonthlyStatusLogStats
     * - StatusLog 테이블의 createdAt을 기준으로 연/월별 등록 수를 집계
     * - 프론트 대시보드에 그래프나 차트용으로 활용 가능
     */
    public List<MonthlyStatDto> getMonthlyStatusLogCount() {
        return statusLogRepository.getMonthlyStatusLogCount();
    }

    /**
     * [1-4] 카테고리별 상태로그 수 (REGISTER, ANSWER, FREE_SHARE)
     * AdminStatsController: getCategoryLogStats
     * - 카테고리별로 상태 로그의 개수를 집계하여 반환
     * - 각 카테고리의 로그 수를 시각화하여 운영 대시보드에 표시
     */
    public List<CategoryLogCountDto> getLogCountByCategory() {
        return statusLogRepository.countLogsGroupByType();
    }

    /**
     * [1-5] 월별 사용자 가입 통계
     * AdminStatsController: getMonthlyUserStats
     * - 사용자 가입 시점(createdAt)을 기준으로 연/월별 가입 수를 집계
     * - 프론트 대시보드에서 사용자 성장 추세를 파악하는 데 사용
     */
    public List<MonthlyUserStatDto> getMonthlyUserSignUpStats() {
        return userRepository.countMonthlySignUps();
    }

    /**
     * [1-6] 신고 Top 10 유저 (가장 많이 신고당한 사용자)
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
     * [1-7] 기여 Top 10 유저 (가장 많은 StatusLog 작성 사용자)
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
}
