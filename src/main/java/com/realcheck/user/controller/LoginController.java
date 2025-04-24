package com.realcheck.user.controller;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * LoginController
 * - 전통적인 Form 기반 로그인/로그아웃 처리 전담 컨트롤러
 * - 로그인 실패 시 쿼리 파라미터로 에러 메시지 전달
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    // ─────────────────────────────────────────────
    // [1] 로그인 페이지 진입
    // ─────────────────────────────────────────────

    /**
     * [1-1] 로그인 페이지 진입
     * - 쿼리 파라미터에 error가 있으면 실패 메시지 전달
     *
     * @param error 로그인 실패 여부를 판단하는 쿼리 파라미터 (예: /login?error=1)
     * @param model 뷰로 메시지를 전달하기 위한 Spring Model 객체
     * @return 로그인 JSP 페이지 뷰 경로
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return "user/login"; // JSP 경로: /WEB-INF/views/user/login.jsp
    }

    // ─────────────────────────────────────────────
    // [2] 로그인 처리
    // ─────────────────────────────────────────────

    /**
     * [2-1] 로그인 POST 처리
     * - 이메일/비밀번호 검증 후 세션 저장
     * - 실패 시 쿼리 파라미터로 에러 전달
     *
     * @param email 입력한 이메일
     * @param password 입력한 비밀번호
     * @param session HttpSession 객체 (세션 저장소)
     * @return 메인 페이지로 리다이렉트 or 실패 시 로그인 페이지로 리다이렉트
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session) {
        try {
            UserDto loginUser = userService.login(email, password);
            session.setAttribute("loginUser", loginUser); // 세션 저장
            return "redirect:/"; // 로그인 성공 → 메인 페이지
        } catch (RuntimeException e) {
            return "redirect:/login?error=1"; // 실패 → 쿼리 파라미터 전달
        }
    }

    // ─────────────────────────────────────────────
    // [3] 로그아웃 처리
    // ─────────────────────────────────────────────

    /**
     * [3-1] 로그아웃 처리
     * - 세션 무효화 후 로그인 페이지로 이동
     *
     * @param session 현재 로그인된 세션
     * @return 로그인 페이지로 리다이렉트
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/login";
    }
}
