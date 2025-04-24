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

    // 의존성 주입된 UserService 객체 (회원 관련 로직을 담당)
    private final UserService userService;

    // ─────────────────────────────────────────────
    // [1] 회원가입 및 로그인 기능
    // ─────────────────────────────────────────────

    /**
     * [1-1] 회원가입 API
     * - 이메일/닉네임 중복 검사 후 회원 등록
     * - 요청 바디(@RequestBody)에 담긴 JSON 데이터를 UserDto 객체로 자동 매핑
     *
     * @param dto 회원가입 정보 (이메일, 닉네임, 비밀번호 등)
     * @return 성공 메시지를 담은 HTTP 200 OK 응답
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto dto) {
        userService.register(dto); // 회원가입 로직 실행 (UserService에서 처리 - 의존성주입 객체 실행)
        return ResponseEntity.ok("회원가입 성공!"); // 클라이언트에 응답
    }

    /**
     * [1-2] 이메일 중복 체크 API
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);

        return ResponseEntity.ok(exists);
    }

    /**
     * [1-3] 닉네임 중복 체크 API
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userService.isNicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }

    // ─────────────────────────────────────────────
    // [2] 마이페이지 및 사용자 정보 수정
    // ─────────────────────────────────────────────

    /**
     * [2-1] 마이페이지 - 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> myPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null) {
            return ResponseEntity.status(401).body(null); // 로그인 안 된 상태 → 401 Unauthorized
        }

        return ResponseEntity.ok(loginUser); // 로그인된 사용자 정보 반환
    }

    /**
     * [2-2] 프로필 수정 API (닉네임/비밀번호)
     * - 로그인된 사용자만 본인의 닉네임 또는 비밀번호를 수정할 수 있음
     * - 세션에서 로그인된 사용자 정보를 가져와 해당 ID 기준으로 업데이트 진행
     *
     * @param dto     클라이언트로부터 받은 수정할 데이터 (nickname, password 등)
     * @param session 현재 로그인된 사용자의 세션 (로그인 정보 보유)
     * @return 수정 성공 메시지 or 로그인 필요 응답
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
     * [2-3] 비밀번호 변경 전용 API
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
