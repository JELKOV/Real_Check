package com.realcheck.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 * Spring Web MVC 설정을 담당하는 클래스
 * 정적 리소스 매핑, 인터셉터 등록, CORS 설정 등을 처리
 */
@Configuration // 이 클래스가 스프링 설정 클래스임을 명시
public class WebConfig implements WebMvcConfigurer {

    // 탈퇴 예약 상태 사용자 차단을 위한 인터셉터
    private final AccountRestrictionInterceptor accountRestrictionInterceptor;

    /**
     * 생성자 주입 (Dependency Injection)
     * AccountRestrictionInterceptor 객체를 생성자 주입으로 받아옴
     */
    @Autowired
    public WebConfig(AccountRestrictionInterceptor accountRestrictionInterceptor) {
        this.accountRestrictionInterceptor = accountRestrictionInterceptor;
    }

    /**
     * [1] 인터셉터 등록
     * AccountRestrictionInterceptor를 모든 요청 경로에 적용
     * 지정된 경로는 예외 처리 (인터셉터 적용 제외)
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(accountRestrictionInterceptor) // 인터셉터 등록
                .addPathPatterns("/**") // 모든 요청에 대해 인터셉터 적용
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/register",
                        "/logout",
                        "/account-restricted",
                        "/cancel-account-deletion",
                        "/css/**",
                        "/js/**",
                        "/images/**"); // 로그인, 회원가입, 로그아웃, 탈퇴 취소 페이지는 예외
    }

    /**
     * [2] 정적 리소스 매핑 설정
     * 웹에서 /uploads/** 경로로 요청되는 파일을 실제 서버 디렉토리에서 제공
     * 파일 서버 기능을 사용하여 이미지, 파일 등을 직접 제공 가능
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // 클라이언트 요청 경로 패턴
                .addResourceLocations("file:uploads/"); // 서버 파일 시스템 경로
    }

    /**
     * [3] CORS (Cross-Origin Resource Sharing) 설정 - naver 
     * 클라이언트가 다른 도메인에서 서버 API를 호출할 수 있도록 허용
     * 여기서는 localhost:8080에서 /api/reverse-geocode API 호출을 허용
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/reverse-geocode") // 특정 API에 CORS 적용
                .allowedOrigins("http://localhost:8080") // 허용할 클라이언트 도메인
                .allowedMethods("GET") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 인증 정보 (쿠키) 허용
    }
}
