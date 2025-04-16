package com.realcheck.user.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 사용자 관리 서비스 클래스
 * - 차단된 사용자 조회 등 관리자 기능 처리
 */
@Service // Spring이 이 클래스를 서비스 빈으로 등록하도록 지정
@RequiredArgsConstructor // 생성자 자동 생성 (final 필드에 대한 생성자 주입 처리)
public class UserAdminService {

    // 사용자 정보를 가져오기 위한 JPA 리포지토리
    private final UserRepository userRepository;

    /**
     * 비활성화된(차단된) 사용자 목록을 조회하는 메서드
     *
     * - 사용자 엔티티에서 isActive가 false인 사용자만 필터링
     * - 엔티티를 UserDto로 변환하여 컨트롤러로 전달
     *
     * @return 비활성화된 사용자 목록 (UserDto 리스트)
     */
    public List<UserDto> getBlockedUsers() {
        return userRepository.findByIsActiveFalse().stream() // 비활성 사용자만 조회
                .map(UserDto::fromEntity) // Entity → DTO로 변환
                .toList(); // 리스트로 반환
    }
}
