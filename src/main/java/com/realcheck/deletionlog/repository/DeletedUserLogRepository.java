package com.realcheck.deletionlog.repository;

import com.realcheck.admin.dto.MonthlyUserStatDto;
import com.realcheck.deletionlog.entity.DeletedUserLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * DeletedUserLogRepository
 * - 탈퇴한 사용자의 기록을 저장하고 통계 처리하는 리포지토리
 */
public interface DeletedUserLogRepository extends JpaRepository<DeletedUserLog, Long> {

    /**
     * [1] 월별 탈퇴 사용자 수 통계
     * AdminStatsService: getMonthlyUserDeletionStats
     * - deletedAt 기준으로 연도/월을 그룹화하여 통계 반환
     * - 관리자 대시보드 통계용
     */
    @Query("""
                SELECT new com.realcheck.admin.dto.MonthlyUserStatDto(
                    YEAR(d.deletedAt), MONTH(d.deletedAt), COUNT(d)
                )
                FROM DeletedUserLog d
                GROUP BY YEAR(d.deletedAt), MONTH(d.deletedAt)
                ORDER BY YEAR(d.deletedAt), MONTH(d.deletedAt)
            """)
    List<MonthlyUserStatDto> countMonthlyDeletedUsers();
}
