package com.realcheck.user.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * UserAdminService
 * - 관리자 전용 사용자 관리 기능
 * (차단 해제, 사용자 검색, 차단 사용자 목록 조회 등)
 */
@Service // Spring이 이 클래스를 서비스 빈으로 등록하도록 지정
@RequiredArgsConstructor // 생성자 자동 생성 (final 필드에 대한 생성자 주입 처리)
public class UserAdminService {

    // 사용자 정보를 가져오기 위한 JPA 리포지토리
    private final UserRepository userRepository;

    // ────────────────────────────────────────
    // [1] 차단 사용자 관련 기능
    // ────────────────────────────────────────

    /**
     * UserAdminController: getBlockedUsers
     * [1] 비활성화된 사용자 목록 조회
     * - 사용자 엔티티에서 isActive가 false인 사용자만 필터링
     * - 엔티티를 UserDto로 변환하여 컨트롤러로 전달
     */
    public List<UserDto> getBlockedUsers() {
        return userRepository.findByIsActiveFalse().stream() // 비활성 사용자만 조회
                .map(UserDto::fromEntity) // Entity → DTO로 변환
                .toList(); // 리스트로 반환
    }

    /**
     * UserAdminController: unblockUser
     * [2] 사용자 차단 해제 기능
     * - isActive = true 로 변경하여 활성화
     */
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        user.setActive(true);
        userRepository.save(user);
    }

    // ────────────────────────────────────────
    // [2] 사용자 검색 기능
    // ────────────────────────────────────────

    /**
     * UserAdminController: searchUsers
     * [1] 이메일 또는 닉네임으로 사용자 검색
     * - keyword가 포함된 사용자 리스트 반환
     */
    public List<UserDto> searchUsers(String keyword) {
        return userRepository.searchByEmailOrNickname(keyword).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }
}
