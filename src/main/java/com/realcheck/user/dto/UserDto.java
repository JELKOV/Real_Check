package com.realcheck.user.dto;

import com.realcheck.user.entity.User;
import lombok.*;

/**
 * UserDto 클래스
 * - 클라이언트 ↔ 서버 간에 사용자 데이터를 전달할 때 사용하는 객체
 * - Entity와 달리 DB와 직접 연관되지 않으며, 필요한 필드만 포함함
 * - API 요청/응답 시 보안과 편의성 모두를 위해 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    // ─────────────────────────────────────────────
    // [1] 사용자 정보 필드
    // ─────────────────────────────────────────────

    // 사용자 id
    private Long id;
    // 사용자 이메일
    private String email;
    // 사용자 닉네임
    private String nickname;
    // 사용자 역활 (권한)
    private String role;
    // 사용자 활성상태
    private boolean isActive;
    // 사용자 비밀번호
    private String password;

    // ─────────────────────────────────────────────
    // [2] 생성자 (비밀번호 제외)
    // 비밀번호가 없는 생성자 (로그인/조회용)
    // ─────────────────────────────────────────────

    /**
     * [2-1] 사용자 정보 생성자 (비밀번호 제외)
     * Entity → DTO 변환 시 사용
     */
    public UserDto(Long id, String email, String nickname, String role, boolean isActive) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.isActive = isActive;
    }

    // ─────────────────────────────────────────────
    // [3] Entity 변환 메서드 (DTO ↔ Entity)
    // ─────────────────────────────────────────────

    /**
     * [3-1] DTO → Entity 변환 메서드
     * - UserDto 객체를 User 엔티티로 변환
     * - 주로 회원가입이나 사용자 정보 수정 시 사용
     */
    public User toEntity() {
        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPassword(password);
        return user;
    }

    /**
     * [3-2] Entity → DTO 변환 메서드
     * - User 엔티티를 UserDto로 변환
     * - DB에서 조회된 사용자 정보를 클라이언트로 전달할 때 사용
     */
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.isActive());
    }

}
