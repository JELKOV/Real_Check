package com.realcheck.user.controller;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;

import jakarta.servlet.http.HttpSession;
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

    // ─────────────────────────────────────────────
    // [1] 회원가입 페이지 이동
    // ─────────────────────────────────────────────

    /**
     * common/header.jsp
     * [1-1] 회원가입 페이지 이동
     */
    @GetMapping("/register")
    public String showRegisterForm(HttpSession session, Model model) {
        // 세션에서 에러 메시지 확인
        String errorMsg = (String) session.getAttribute("errorMsg");
        if (errorMsg != null) {
            model.addAttribute("errorMsg", errorMsg);
            session.removeAttribute("errorMsg");
        }
        return "user/register";
    }

    // ─────────────────────────────────────────────
    // [2] 회원가입 처리 (폼 기반)
    // ─────────────────────────────────────────────

    /**
     * page: user/register.jsp
     * [2] 회원가입 처리
     * 비밀번호 확인 매칭
     */
    @PostMapping("/register")
    public String register(@RequestParam String email,
            @RequestParam String nickname,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session) {
        // 1) 비밀번호 일치 검사
        if (!password.equals(confirmPassword)) {
            session.setAttribute("errorMsg", "비밀번호와 확인이 일치하지 않습니다.");
            return "redirect:/register";
        }

        try {
            UserDto dto = new UserDto(
                    null, // id
                    email, // email
                    nickname, // nickname
                    "USER", // role
                    true, // isActive
                    0, // points
                    0, // reportCount
                    false, // isPendingDeletion
                    null, // deletionScheduledAt
                    null, // createdAt
                    null, // updatedAt
                    null, // lastLogin
                    0 // version (최초 가입 시 0)
            );
            userService.register(dto, password);
            session.setAttribute("successMsg", "🎉 회원가입이 완료되었습니다! 환영합니다, " + nickname + "님!");
            return "redirect:/login";
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("이메일")) {
                session.setAttribute("errorMsg", "이미 사용 중인 이메일입니다.");
            } else if (msg.contains("닉네임")) {
                session.setAttribute("errorMsg", "이미 사용 중인 닉네임입니다.");
            } else {
                session.setAttribute("errorMsg", "알 수 없는 오류가 발생했습니다.");
            }
            return "redirect:/register";
        }
    }

}
