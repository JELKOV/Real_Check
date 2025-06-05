package com.realcheck.user.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.report.dto.ReportDto;
import com.realcheck.report.repository.ReportRepository;
import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.repository.RequestRepository;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * UserAdminService
 * - 관리자 전용 사용자 관리 기능
 * - (차단 해제, 사용자 검색, 차단 사용자 목록 조회 등)
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final StatusLogRepository statusLogRepository;
    private final RequestRepository requestRepository;
    private final ReportRepository reportRepository;

    // ────────────────────────────────────────
    // [1] 차단 사용자 관련 기능
    // ────────────────────────────────────────

    /**
     * [1-1] 비활성화된 사용자 목록 조회
     * UserAdminController: getBlockedUsers
     * - 사용자 엔티티에서 isActive가 false인 사용자만 필터링
     * - 엔티티를 UserDto로 변환하여 컨트롤러로 전달
     */
    public List<UserDto> getBlockedUsers() {
        return userRepository.findByIsActiveFalse().stream() // 비활성 사용자만 조회
                .map(UserDto::fromEntity) // Entity → DTO로 변환
                .toList(); // 리스트로 반환
    }

    /**
     * [1-2] 사용자 차단 기능
     * UserAdminController: blockUser
     * - isActive = false 로 변경하여 비활성화
     * - 추후 확장: 관리자 행동 로그 기록
     */
    @Transactional
    public void blockUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.isActive()) {
            throw new IllegalStateException("이미 차단된 사용자입니다.");
        }

        user.setActive(false); // 차단 처리

        // 추후 확장: 로그 기록
        // adminActionLogRepository.save(AdminActionLog.block(adminId, userId));
    }

    /**
     * [1-3] 사용자 차단 해제 기능
     * UserAdminController: unblockUser
     * - isActive = true 로 변경하여 활성화
     */
    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        user.setActive(true);
        userRepository.save(user);
    }

    // ────────────────────────────────────────
    // [2] 사용자 검색 기능
    // ────────────────────────────────────────

    /**
     * [2-1] 사용자 검색 (이메일 또는 닉네임)
     * UserAdminController: searchUsers
     * - keyword가 포함된 사용자 리스트 반환
     */
    public List<UserDto> searchUsers(String keyword) {
        return userRepository.searchByEmailOrNickname(keyword).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * [2-2] 사용자 단건 조회
     * UserAdminController: getUserDetails
     * - 사용자 ID로 단건 조회 후 UserDto로 변환하여 반환
     */
    public UserDto getUserDto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserDto.fromEntity(user);
    }

    /**
     * [2-3] 사용자 상태 로그 조회 (페이지네이션 + 필터링)
     * UserAdminController: getUserStatusLogs
     * - 관리자가 특정 사용자의 활동 이력을 확인하기 위한 상태 로그 목록
     * - type 필터가 존재하면 해당 StatusType만 필터링
     */
    public Page<StatusLogDto> getUserStatusLogs(Long userId, int page, int size, String type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (type != null && !type.isEmpty()) {
            StatusType statusType;
            try {
                statusType = StatusType.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("올바르지 않은 statusType: " + type);
            }
            return statusLogRepository.findByReporterIdAndStatusType(userId, statusType, pageable)
                    .map(StatusLogDto::fromEntity);
        }

        return statusLogRepository.findByReporterId(userId, pageable)
                .map(StatusLogDto::fromEntity);
    }

    /**
     * [2-4] 사용자 등록 요청 목록 조회 (페이지네이션)
     * UserAdminController: getUserRequests
     * - 관리자가 특정 사용자가 작성한 요청(Request) 목록 확인
     */
    public Page<RequestDto> getUserRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return requestRepository.findByUserId(userId, pageable)
                .map(r -> RequestDto.fromEntity(r, 0)); // visibleCount = 0
    }

    /**
     * [2-5] 사용자 신고 목록 조회 (페이지네이션)
     * UserAdminController: getUserReports
     * - 관리자가 특정 사용자가 작성한 신고 내역 확인
     */
    public Page<ReportDto> getUserReports(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reportRepository.findByReporterId(userId, pageable)
                .map(ReportDto::fromEntity);
    }

    /**
     * [2-6] 신고된 사용자 목록 조회 (신고 횟수 1회 이상) [미사용]
     * UserAdminController: getReportedUsers
     * - 신고 받은 사용자는 StatusLog를 신고 받은 사용자
     * - 신고된 사용자와 신고 횟수를 함께 반환
     */
    public List<UserDto> getReportedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getReportCount() > 0) // 신고 횟수 1회 이상 필터링
                .map(UserDto::fromEntity)
                .toList();
    }
}
