package com.realcheck.user.controller;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;
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

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto dto) {
        UserDto loginUser = userService.login(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(loginUser);
    }
}
