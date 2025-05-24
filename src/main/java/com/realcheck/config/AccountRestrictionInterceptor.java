package com.realcheck.config;

import com.realcheck.user.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * AccountRestrictionInterceptor
 * - 탈퇴 예약 상태 사용자에 대한 서비스 이용 제한을 처리하는 인터셉터
 * - 로그인된 사용자 중 탈퇴 예약된 사용자가 서비스 페이지에 접근할 수 없도록 제어
 * - HandlerInterceptor 인터페이스 구현을 통해 각 요청 전/후 처리 가능
 */
@Component
public class AccountRestrictionInterceptor implements HandlerInterceptor {

    /**
     * [1] preHandle (요청 전 처리)
     * - 클라이언트 요청이 컨트롤러에 도달하기 전에 실행
     * - 탈퇴 예약 상태인 사용자 (로그인된 사용자)가 서비스 페이지에 접근할 수 없도록 제한
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        // 세션에서 로그인된 사용자 정보 확인
        HttpSession session = request.getSession();
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인된 사용자가 존재하고, 탈퇴 예약 상태라면 접근 차단
        if (loginUser != null && loginUser.isPendingDeletion()) {
            // 로그인된 사용자이면서 탈퇴 예약 상태일 경우
            response.sendRedirect("/account-restricted");
            return false; // 요청을 차단하고 리디렉트
        }

        return true; // 정상적으로 요청 진행
    }

    /**
     * [2] postHandle (요청 처리 후, 응답 전 처리)
     * - 컨트롤러 메서드가 실행된 후, View가 렌더링되기 전에 실행
     * - 현재는 특별한 후처리 로직을 수행하지 않음
     */
    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
        // 여기서는 특별한 로직 필요 없음
    }

    /**
     * [3] afterCompletion (응답 완료 후 처리)
     * - 클라이언트에게 응답이 전송된 후 실행
     * - 예외 발생 여부와 관계없이 항상 실행됨
     * - 주로 로깅, 자원 정리 등 후처리 로직에 사용
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler, @Nullable Exception ex)
            throws Exception {
        // 여기서도 특별한 로직 필요 없음
    }
}
