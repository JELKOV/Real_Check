package com.realcheck.admin.controller;

import com.realcheck.admin.dto.PointDailyAggregateDto;
import com.realcheck.admin.dto.PointHistoryDto;
import com.realcheck.admin.dto.PointZeroRatioDto;
import com.realcheck.admin.dto.PointBalanceDistributionDto;
import com.realcheck.admin.dto.TopUserByPointsDto;
import com.realcheck.admin.service.AdminPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointController {

    private final AdminPointService adminPointService;

    /**
     * [1] 일별 포인트 합계 리스트
     * page: admin/stats.jsp
     * - 포인트 지급 내역을 일별로 집계하여 반환
     * - 예: [{ "date": "2023-10-01", "totalPoints": 1000 }, ...]
     * - 반환 형식: List<PointDailyAggregateDto>
     */
    @GetMapping("/daily-aggregate")
    public ResponseEntity<List<PointDailyAggregateDto>> getDailyAggregate() {
        List<PointDailyAggregateDto> list = adminPointService.getDailyAggregatedPoints();
        return ResponseEntity.ok(list);
    }

    /**
     * [2] 최근 포인트 내역 (limit 기본값 10)
     * page: admin/stats.jsp
     * - 최근 포인트 지급 내역을 조회하여 반환
     * - 반환 형식: List<PointHistoryDto>
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PointHistoryDto>> getRecentPoints(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<PointHistoryDto> list = adminPointService.getRecentPointHistory(limit);
        return ResponseEntity.ok(list);
    }

    /**
     * [3] 포인트 분포도 (양수/0/사용자 수)
     * page: admin/stats.jsp
     * - 사용자 잔액 분포를 조회하여 반환
     * - 반환 형식: PointBalanceDistributionDto
     */
    @GetMapping("/distribution")
    public ResponseEntity<PointBalanceDistributionDto> getBalanceDistribution() {
        PointBalanceDistributionDto dto = adminPointService.getPointBalanceDistribution();
        return ResponseEntity.ok(dto);
    }

    /**
     * [4] Top N 잔액 보유 사용자
     * page: admin/stats.jsp
     * - 잔액이 가장 높은 사용자 N명을 조회하여 반환
     * - limit 파라미터로 반환할 사용자 수를 조정 (기본값 10)
     * - 반환 형식: List<TopUserByPointsDto>
     */
    @GetMapping("/top")
    public ResponseEntity<List<TopUserByPointsDto>> getTopUsers(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<TopUserByPointsDto> list = adminPointService.getTopUsersByPoints(limit);
        return ResponseEntity.ok(list);
    }

    /**
     * [5] 잔액 0원인 사용자 비율 조회
     * page: admin/stats.jsp
     * - 전체 사용자 중 잔액이 0원인 사용자의 비율을 조회하여 반환
     * - 반환 형식: PointZeroRatioDto
     */
    @GetMapping("/zero/ratio")
    public ResponseEntity<PointZeroRatioDto> getZeroBalanceRatio() {
        return ResponseEntity.ok(adminPointService.getZeroBalanceRatio());
    }
}
