package com.realcheck.user.controller;

import com.realcheck.user.dto.UserDto;
import com.realcheck.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * LoginController (ALL DONE)
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
     * page: common/header.jsp
     * page: user/register.jsp
     * - 쿼리 파라미터에 error가 있으면 실패 메시지 전달
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model, HttpSession session) {
        if (error != null) {
            model.addAttribute("errorMsg", "이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 회원가입 성공 메시지 세션에서 읽기
        String successMsg = (String) session.getAttribute("successMsg");
        if (successMsg != null) {
            model.addAttribute("successMsg", successMsg);
            session.removeAttribute("successMsg");
        }

        return "user/login";
    }

    // ─────────────────────────────────────────────
    // [2] 로그인 처리
    // ─────────────────────────────────────────────

    /**
     * [2-1] 로그인 POST 처리
     * page: user/login.jsp
     * - 이메일/비밀번호 검증 후 세션 저장
     * - 실패 시 쿼리 파라미터로 에러 전달
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            UserDto loginUser = userService.login(email, password);

            // (1) 세션에 로그인 사용자 저장
            session.setAttribute("loginUser", loginUser);

            // (2) 탈퇴 예약 상태 확인 (서비스 접근 차단)
            if (loginUser.isPendingDeletion()) {
                return "redirect:/account-restricted"; // 서비스 접근 제한 페이지로 이동
            }

            // (3) 정상 로그인 처리
            return "redirect:/"; // 로그인 성공 → 메인 페이지

        } catch (RuntimeException e) {
            // 에러 메시지 전달
            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    // ─────────────────────────────────────────────
    // [3] 로그아웃 처리 (POST)
    // ─────────────────────────────────────────────

    /**
     * [3-1] 로그아웃 처리
     * page: common/header.jsp
     * - 세션 무효화 후 로그인 페이지로 이동
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/login";
    }

    // ─────────────────────────────────────────────
    // [4] 회원 탈퇴 관련 API
    // ─────────────────────────────────────────────

    /**
     * [4-1] 회원 탈퇴 처리
     * page: mypage.jsp
     */
    @GetMapping("/delete-account")
    public String requestAccountDeletion(HttpSession session, RedirectAttributes redirectAttributes) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        userService.requestAccountDeletion(loginUser.getId());
        session.invalidate(); // 로그아웃 처리

        // 플래시 메시지 설정 (메인 페이지에서 확인)
        redirectAttributes.addFlashAttribute("deletionRequested", true);
        return "redirect:/";
    }

    /**
     * [4-2] 탈퇴 예약 취소 처리
     * page: account-restricted.jsp
     * - 사용자 탈퇴 예약을 취소하고 세션 정보를 갱신
     */
    @GetMapping("/cancel-account-deletion")
    public String cancelAccountDeletion(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 탈퇴 예약 취소 처리
        userService.cancelAccountDeletion(loginUser.getId());

        // 세션 사용자 정보 갱신
        UserDto updatedUser = userService.getUserDtoById(loginUser.getId());
        session.setAttribute("loginUser", updatedUser);

        return "redirect:/?cancel_success=true";
    }
}
