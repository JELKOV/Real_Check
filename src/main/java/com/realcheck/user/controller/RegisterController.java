package com.realcheck.user.controller;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * RegisterController
 * - GET /register : 회원가입 폼
 * - POST /register : 실제 가입 처리 (폼 기반)
 */
@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    /**
     * [GET] 회원가입 페이지
     * 
     * @param error (optional) 오류 코드
     */
    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String error,
            Model model) {
        if (error != null) {
            // error 파라미터별로 다른 메시지 매핑 가능
            String msg = switch (error) {
                case "password" -> "비밀번호와 확인이 일치하지 않습니다.";
                case "dupEmail" -> "이미 사용 중인 이메일입니다.";
                case "dupNick" -> "이미 사용 중인 닉네임입니다.";
                default -> "알 수 없는 오류가 발생했습니다.";
            };
            model.addAttribute("errorMsg", msg);
        }
        return "user/register"; // /WEB-INF/views/user/register.jsp
    }

    /**
     * [POST] 회원가입 처리
     * - 비밀번호 확인 매칭
     * - UserService.register 호출
     */
    @PostMapping("/register")
    public String register(@RequestParam String email,
            @RequestParam String nickname,
            @RequestParam String password,
            @RequestParam String confirmPassword) {
        // 1) 비밀번호 일치 검사
        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error=password";
        }

        // 2) 가입 시도
        try {
            UserDto dto = new UserDto(null, email, nickname, null, true, password);
            userService.register(dto);
            return "redirect:/login"; // 성공 시 로그인 페이지로
        } catch (RuntimeException e) {
            // service에서 던진 메시지에 따라 분기
            String msg = e.getMessage();
            if (msg.contains("이메일")) {
                return "redirect:/register?error=dupEmail";
            } else if (msg.contains("닉네임")) {
                return "redirect:/register?error=dupNick";
            }
            return "redirect:/register?error";
        }
    }
}
