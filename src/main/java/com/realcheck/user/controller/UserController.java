package com.realcheck.user.controller;

import com.realcheck.user.dto.PasswordUpdateRequestDto;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController 클래스
 * - 사용자 관련 HTTP 요청을 처리하는 컨트롤러
 * - 클라이언트(프론트엔드)로부터 요청을 받아 서비스(Service) 계층으로 전달
 */

@RestController // 이 클래스는 REST API를 처리하는 컨트롤러임을 명시 (응답은 JSON 형식으로 반환)
@RequestMapping("/api/user") // 이 컨트롤러의 모든 API는 "/api/user"로 시작하는 경로를 가짐
@RequiredArgsConstructor // final로 선언된 필드를 자동으로 생성자 주입 (DI)해주는 Lombok 어노테이션
public class UserController {

    // 의존성 주입된 UserService 객체
    private final UserService userService;

    /**
     * page: user/register.jsp
     * [1] 이메일 중복 체크 API
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);

        return ResponseEntity.ok(exists);
    }

    /**
     * page: user/register.jsp
     * page: user/edit-profile.jsp
     * [2] 닉네임 중복 체크 API
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userService.isNicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }

    // ─────────────────────────────────────────────
    // [2] 마이페이지 사용자 정보 수정
    // ─────────────────────────────────────────────


    /**
     * [1] 프로필 수정 API (닉네임/비밀번호)
     * - 로그인된 사용자만 본인의 닉네임 또는 비밀번호를 수정할 수 있음
     * - 세션에서 로그인된 사용자 정보를 가져와 해당 ID 기준으로 업데이트 진행
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody UserDto dto, HttpSession session) {
        // 세션에서 로그인한 사용자 정보 조회
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        // 로그인하지 않은 경우 → 401 Unauthorized 반환
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        // 로그인된 사용자의 ID로 수정 요청
        userService.updateProfile(loginUser.getId(), dto);
        // 수정 완료 메시지 반환
        return ResponseEntity.ok("수정 완료");
    }

    /**
     * page: user/change-password.jsp
     * [2] 비밀번호 변경 전용 API
     * - 현재 비밀번호 확인 후 새 비밀번호로 교체
     */
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordUpdateRequestDto dto, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            userService.changePassword(loginUser.getId(), dto);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
