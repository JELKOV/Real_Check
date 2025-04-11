package com.realcheck.user.dto;

import com.realcheck.user.entity.User;
import lombok.*;

/**
 * UserDto 클래스
 * - 클라이언트 ↔ 서버 간에 사용자 데이터를 전달할 때 사용하는 객체
 * - Entity와 달리 DB와 직접 연관되지 않으며, 필요한 필드만 포함함
 * - API 요청/응답 시 보안과 편의성 모두를 위해 사용
 */
@Data // Getter, Setter, equals, hashCode, toString 자동 생성
@NoArgsConstructor // 파라미터 없는 생성자 생성
@AllArgsConstructor // 모든 필드를 포함한 생성자 생성
public class UserDto {

    private Long id;
    private String email;
    private String nickname;
    private String role;
    private boolean isActive;
    private String password;

    public UserDto(Long id, String email, String nickname, String role, boolean isActive) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.isActive = isActive;
    }
    
    // fromEntity 메서드에서 사용
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.isActive());
    }

    /**
     * DTO → Entity로 변환하는 메서드
     * - Service 계층에서 DB 저장 전에 사용
     */
    public User toEntity() {
        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPassword(password);
        return user;
    }

}
