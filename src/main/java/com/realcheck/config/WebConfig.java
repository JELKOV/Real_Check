package com.realcheck.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 * - 정적 리소스(Static Resource) 경로 설정을 담당하는 Spring Configuration 클래스
 * - 주로 /uploads/** 요청을 실제 파일 시스템 uploads 폴더로 매핑하는 역할을 함
 */
@Configuration // 이 클래스가 스프링 설정 클래스임을 명시
public class WebConfig implements WebMvcConfigurer {

    /**
     * 정적 리소스 매핑 설정
     * - /uploads/** 로 들어오는 URL 요청을
     *   프로젝트 루트 디렉토리의 /uploads/ 폴더 안 파일로 매핑함
     * - 예시:
     *   - /uploads/test.png 요청 → 실제 서버의 uploads/test.png 파일 반환
     *
     * @param registry ResourceHandlerRegistry 스프링이 제공하는 리소스 등록 객체
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // 웹 요청 패턴
                .addResourceLocations("file:uploads/"); // 실제 물리 경로
    }
}
