package com.realcheck.status.repository;

import com.realcheck.status.entity.StatusLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * StatusLogRepository
 * - DB와 통신하는 계층 (JPA Repository)
 * - 기본 CRUD + 장소별/사용자별 조건 조회 기능 제공
 */
public interface StatusLogRepository extends JpaRepository<StatusLog, Long> {

    // 특정 장소(placeId)에 대한 상태 로그를 최신순으로 조회
    List<StatusLog> findByPlaceIdOrderByCreatedAtDesc(Long placeId);

    // 특정 사용자의 당일 등록 횟수를 조회 (포인트 지급 조건 등 확인용)
    int countByReporterIdAndCreatedAtBetween(Long reporterId, LocalDateTime start, LocalDateTime end);
}