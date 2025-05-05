package com.realcheck.admin.service;

import com.realcheck.admin.dto.MonthlyStatDto;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.repository.StatusLogRepository;
import lombok.RequiredArgsConstructor;
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

    /**
     * AdminStatsController: getTotalReportCount
     * [1] 전체 신고 수 반환
     * - report 테이블의 전체 레코드 수를 카운트
     * - 관리자 대시보드에서 신고 누적 현황을 파악할 때 사용
     */
    public long getTotalReportCount() {
        return reportRepository.count();
    }

    /**
     * AdminStatsController: getTotalPointSum
     * [2] 전체 포인트 합계 반환
     * - 지급된 포인트 총합을 반환
     * - null이 반환될 경우 0으로 처리 (SUM 함수는 결과가 없으면 null 반환 가능성 있음)
     *
     * @return int 전체 포인트 합계
     */
    public int getTotalPointSum() {
        Integer result = pointRepository.sumAllPoints();
        return result != null ? result : 0;
    }

    /**
     * AdminStatsController: getMonthlyStatusLogStats
     * [3] 월별 상태 로그 등록 수 통계 반환
     * - StatusLog 테이블의 createdAt을 기준으로 연/월별 등록 수를 집계
     * - 프론트 대시보드에 그래프나 차트용으로 활용 가능
     */
    public List<MonthlyStatDto> getMonthlyStatusLogCount() {
        return statusLogRepository.getMonthlyStatusLogCount();
    }
}
