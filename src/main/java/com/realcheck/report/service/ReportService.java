package com.realcheck.report.service;

import org.springframework.stereotype.Service;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.entity.Report;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service // 비즈니스 로직을 수행하는 서비스 계층
@RequiredArgsConstructor // 생성자 주입을 자동으로 처리
public class ReportService {
    private final ReportRepository reportRepository;
    private final StatusLogRepository statusLogRepository;
    private final UserRepository userRepository;

    /**
     * 신고 처리 메서드
     * - 신고한 사용자 ID, 신고 대상 로그 ID를 받아 DB에 저장
     *
     * @param userId 신고자 ID (세션에서 가져온 로그인 사용자 ID)
     * @param dto    신고 내용 (신고 사유 + 신고할 StatusLog ID)
     */
    public void report(Long userId, ReportDto dto) {
        // 신고자 유효성 확인
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        // 신고 대상 로그 유효성 확인
        StatusLog log = statusLogRepository.findById(dto.getStatusLogId())
                .orElseThrow(() -> new RuntimeException("해당 상태 정보 없음"));

        // DTO → Entity 변환 후 저장
        Report report = dto.toEntity(reporter, log);
        reportRepository.save(report);
    }
}
