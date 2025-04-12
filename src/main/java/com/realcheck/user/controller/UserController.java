package com.realcheck.user.controller;

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

    /**
     * 회원가입 API
     * - 클라이언트가 POST 방식으로 "/api/user/register"에 요청을 보내면 실행됨
     * - 요청 바디(@RequestBody)에 담긴 JSON 데이터를 UserDto 객체로 자동 매핑
     * - 회원가입 처리 후 성공 메시지를 반환
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
     * 로그인 API
     * - 사용자의 이메일과 비밀번호를 받아 로그인 처리
     * - 성공 시 세션에 사용자 정보를 저장하여 로그인 상태 유지
     *
     * @param dto     로그인 요청 정보 (이메일, 비밀번호)
     * @param session HttpSession: 로그인 성공 시 사용자 정보를 저장할 수 있는 서버 측 저장소
     * @return 로그인된 사용자 정보를 담은 응답
     */

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto dto, HttpSession session) {
        // 이메일 + 비밀번호를 검증하여 로그인 시도
        UserDto loginUser = userService.login(dto.getEmail(), dto.getPassword());

        // 로그인 성공 시, 사용자 정보를 세션에 저장 (세션 키: "loginUser")
        session.setAttribute("loginUser", loginUser);

        // 사용자 정보를 응답으로 반환 (보안상 password는 DTO에 포함 X)
        return ResponseEntity.ok(loginUser);
    }

    // 로그인한 사용자 정보 확인 API (마이페이지용)
    // 추후에 데이터를 가져와야 할경우 Service 연결
    @GetMapping("/me")
    public ResponseEntity<UserDto> myPage(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null) {
            return ResponseEntity.status(401).body(null); // 로그인 안 된 상태 → 401 Unauthorized
        }

        return ResponseEntity.ok(loginUser); // 로그인된 사용자 정보 반환
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 현재 세션을 무효화 (로그아웃 처리)
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
