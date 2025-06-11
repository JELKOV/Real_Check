package com.realcheck.user.controller;

import com.realcheck.user.dto.PasswordUpdateRequestDto;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController 클래스 (ALL DONE)
 * - 사용자 관련 HTTP 요청을 처리하는 컨트롤러
 * - 클라이언트(프론트엔드)로부터 요청을 받아 서비스(Service) 계층으로 전달
 */

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────
    // [1] 사용자 회원가입 / 중복 체크 관련 API
    // ─────────────────────────────────────────────

    /**
     * [1-1] 이메일 중복 체크 API (AJAX)
     * page: user/register.jsp
     * - 사용자가 입력한 이메일이 이미 사용 중인지 확인
     * - 회원가입 시 이메일 중복 확인용
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);

        return ResponseEntity.ok(exists);
    }

    /**
     * [1-2] 닉네임 중복 체크 API
     * page: user/register.jsp
     * page: user/edit-profile.jsp
     * - 사용자가 입력한 닉네임이 이미 사용 중인지 확인
     * - 회원가입 및 프로필 수정 시 닉네임 중복 확인용
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userService.isNicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }

    // ─────────────────────────────────────────────
    // [2] 사용자 정보 수정 (마이페이지) 관련 API
    // ─────────────────────────────────────────────

    /**
     * [2-1] 프로필 수정 API (닉네임/비밀번호)
     * page: user/edit-profile.jsp
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

        try {
            // 로그인된 사용자의 ID로 수정 요청
            userService.updateProfile(loginUser.getId(), dto);
            // 세션 무효화 (로그아웃)
            session.invalidate();
            // 수정 완료 메시지 반환
            return ResponseEntity.ok("수정 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * [2-2] 비밀번호 변경 전용 API
     * page: user/change-password.jsp
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
            // 세션 무효화 (로그아웃)
            session.invalidate();
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
