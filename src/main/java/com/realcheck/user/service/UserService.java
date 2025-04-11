package com.realcheck.user.service;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.entity.User.Role;
import com.realcheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UserService 클래스
 * - 회원가입 등 사용자 관련 비즈니스 로직을 처리
 * - 컨트롤러로부터 호출되어 실제 동작을 수행
 */

@Service // 스프링이 이 클래스를 서비스 컴포넌트로 등록함 (빈으로 자동 생성됨)
@RequiredArgsConstructor // final이 붙은 필드를 자동으로 생성자 주입 (DI)
public class UserService {

    // UserRepository를 주입받음 (DB 접근을 담당)
    private final UserRepository userRepository;

    /**
     * 회원가입 처리 메서드
     * - 이메일/닉네임 중복 검사
     * - DTO를 엔티티로 변환 후 저장
     * 
     * @param dto - 클라이언트로부터 받은 회원가입 정보
     */
    public void register(UserDto dto) {
        // 1. 이메일 중복 검사
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        });
        // 2. 닉네임 중복 검사
        userRepository.findByNickname(dto.getNickname()).ifPresent(u -> {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        });

        // 3. DTO → Entity 변환
        User user = dto.toEntity();

        // 4. 기본 정보 세팅 (역할, 포인트, 활성 상태)
        user.setRole(Role.USER); // 회원가입은 기본적으로 일반 사용자로 설정
        user.setPoints(0); // 기본 포인트는 0
        user.setActive(true); // 계정은 기본적으로 활성화 상태

        // 5. DB에 저장
        userRepository.save(user);
    }
}
