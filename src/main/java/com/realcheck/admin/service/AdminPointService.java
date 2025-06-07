package com.realcheck.admin.service;

import com.realcheck.admin.dto.PointDailyAggregateDto;
import com.realcheck.admin.dto.PointHistoryDto;
import com.realcheck.admin.dto.PointZeroRatioDto;
import com.realcheck.admin.dto.PointBalanceDistributionDto;
import com.realcheck.admin.dto.TopUserByPointsDto;
import com.realcheck.point.repository.PointRepository;
import com.realcheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPointService {
    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    /**
     * [1-1] 일별 포인트 합계 리스트 조회
     * AdminPointController: getDailyAggregate
     * - 포인트 지급 내역을 일별로 집계하여 반환
     * - 반환 형식: List<PointDailyAggregateDto>
     */
    public List<PointDailyAggregateDto> getDailyAggregatedPoints() {
        return pointRepository.findDailyAggregateRaw().stream()
                .map(arr -> {
                    // arr[0] 은 DATE(p.earnedAt) → java.sql.Date거나 java.util.Date
                    java.sql.Date sqlDate = (java.sql.Date) arr[0];
                    Long sum = (Long) arr[1];
                    return new PointDailyAggregateDto(
                            sqlDate.toLocalDate(), // LocalDate 로 변환
                            sum);
                })
                .collect(Collectors.toList());
    }

    /**
     * [1-2] 최근 포인트 내역
     * AdminPointController: getRecentPoints
     * - limit 파라미터로 최근 N개의 포인트 내역을 조회
     * - 반환 형식: List<PointHistoryDto>
     */
    public List<PointHistoryDto> getRecentPointHistory(int limit) {
        return pointRepository
                .findAllByOrderByEarnedAtDesc(PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(p -> {
                    // type 이 null 이면 "UNKNOWN" 등 기본 문자열 사용
                    String typeName = p.getType() != null
                            ? p.getType().name()
                            : "UNKNOWN";
                    return new PointHistoryDto(
                            p.getEarnedAt(),
                            p.getUser().getId(),
                            p.getUser().getNickname(),
                            p.getAmount(),
                            typeName,
                            p.getReason());
                })
                .collect(Collectors.toList());
    }

    /**
     * [1-3] 사용자 잔액 분포
     * AdminPointController: getBalanceDistribution
     * - 사용자 잔액을 양수/0/ 분류하여 카운트
     * - 반환 형식: PointBalanceDistributionDto
     */
    public PointBalanceDistributionDto getPointBalanceDistribution() {
        List<Object[]> raw = userRepository.countUserByPointBalance();
        if (raw.isEmpty()) {
            return new PointBalanceDistributionDto(0, 0);
        }
        Object[] row = raw.get(0);
        long positive = ((Number) row[0]).longValue();
        long zero = ((Number) row[1]).longValue();
        return new PointBalanceDistributionDto(positive, zero);
    }

    /**
     * [1-4] 잔액 높은 Top 10 사용자 (limit 파라미터 무시)
     * AdminPointController: getTopUsers
     * - 잔액이 가장 높은 사용자 10명을 조회
     * - 반환 형식: List<TopUserByPointsDto>
     */
    public List<TopUserByPointsDto> getTopUsersByPoints(int limit) {
        return userRepository.findTop10ByOrderByPointsDesc()
                .stream()
                .map(u -> new TopUserByPointsDto(
                        u.getId(),
                        u.getNickname(),
                        u.getPoints()))
                .collect(Collectors.toList());
    }

    /**
     * [1-5] 잔액 0원 사용자 리스트
     * AdminPointController: getZeroBalanceRatio
     * - 잔액이 0인 사용자의 비율을 계산하여 반환
     */
    public PointZeroRatioDto getZeroBalanceRatio() {
        long zeroCount = userRepository.countByPoints(0);
        long totalCount = userRepository.count();
        double ratio = totalCount == 0 ? 0.0 : (double) zeroCount / totalCount;
        return new PointZeroRatioDto(zeroCount, totalCount, ratio);
    }
}
