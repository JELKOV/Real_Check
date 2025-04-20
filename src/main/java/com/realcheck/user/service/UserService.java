package com.realcheck.user.service;

import com.realcheck.user.dto.PasswordUpdateRequestDto;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.entity.User.Role;
import com.realcheck.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UserService 클래스
 * - 사용자 관련 비즈니스 로직 (회원가입, 로그인, 비밀번호 변경 등)을 처리
 */
@Service // 스프링이 이 클래스를 서비스 컴포넌트로 등록함 (빈으로 자동 생성됨)
@RequiredArgsConstructor // final이 붙은 필드를 자동으로 생성자 주입 (DI)
public class UserService {

    // UserRepository를 주입받음 (DB 접근을 담당)
    private final UserRepository userRepository;

    // 암호화 주입
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // [1] 회원가입 및 로그인 기능
    // ─────────────────────────────────────────────

    /**
     * [1-1] 회원가입 처리
     * - 이메일, 닉네임 중복 확인
     * - 비밀번호 암호화 후 저장
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

        // 4. 비밀번호 암호화 적용
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);

        // 4. 기본 정보 세팅 (역할, 포인트, 활성 상태)
        user.setRole(Role.USER); // 회원가입은 기본적으로 일반 사용자로 설정
        user.setPoints(0); // 기본 포인트는 0
        user.setActive(true); // 계정은 기본적으로 활성화 상태

        // 5. DB에 저장
        userRepository.save(user);
    }

    /**
     * [1-2] 로그인 처리
     * - 이메일로 사용자 조회
     * - 비밀번호 비교 (암호화된 비밀번호와 비교)
     * - 성공 시 DTO 반환
     *
     * @param email       사용자가 입력한 이메일
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @return 로그인에 성공한 사용자 정보를 담은 UserDto (비밀번호는 포함하지 않음)
     */
    public UserDto login(String email, String rawPassword) {
        // 1. 이메일로 사용자 조회 (존재하지 않으면 예외 발생)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 2. 입력한 비밀번호(rawPassword)와 저장된 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 성공 → User 엔티티를 DTO로 변환하여 반환 (비밀번호 제외)
        return UserDto.fromEntity(user);
    }

    // ─────────────────────────────────────────────
    // [2] 사용자 정보 관련 기능 (마이페이지)
    // ─────────────────────────────────────────────

    /**
     * [2-1] 이메일 중복 여부 확인
     */
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * [2-2] 닉네임 중복 여부 확인
     */
    public boolean isNicknameExists(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    /**
     * [2-3] 사용자 프로필 수정
     * - 닉네임 또는 비밀번호 변경
     * - 닉네임은 단순 대체, 비밀번호는 암호화 후 저장
     *
     * @param id  수정할 사용자 ID
     * @param dto 클라이언트에서 전달된 수정 정보 (닉네임, 비밀번호 중 하나 이상)
     */
    public void updateProfile(Long id, UserDto dto) {
        // 1. 해당 ID의 사용자 정보 조회 (없으면 예외 발생)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 2. 닉네임이 전달된 경우 → 기존 닉네임을 새 닉네임으로 변경
        if (dto.getNickname() != null)
            user.setNickname(dto.getNickname());

        // 3. 비밀번호가 전달된 경우 → 암호화 후 저장
        if (dto.getPassword() != null) {
            String encrypted = passwordEncoder.encode(dto.getPassword());
            user.setPassword(encrypted);
        }

        // 4. 변경된 사용자 정보를 DB에 저장
        userRepository.save(user);
    }

    /**
     * [2-4] 비밀번호 변경
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     * 
     * @param userId 로그인된 사용자 ID
     * @param dto    비밀번호 변경 요청 (현재/새 비밀번호)
     */
    public void changePassword(Long userId, PasswordUpdateRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        // 새 비밀번호로 변경 (암호화)
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @PostConstruct
    public void insertAdminAccount() {
        System.out.println("⚙️ insertAdminAccount 실행됨");

        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setNickname("관리자");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setPoints(0);
            admin.setPassword(passwordEncoder.encode("admin1234"));

            userRepository.save(admin);
            System.out.println("✅ 관리자 계정 생성 완료");
        } else {
            System.out.println("ℹ️ 이미 admin@example.com 계정이 존재합니다.");
        }
    }
}
