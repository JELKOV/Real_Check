package com.realcheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 비밀번호 암호화를 위한 Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {

        // // 원하는 비밀번호 (TEST용)
        // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // System.out.println("암호화된 비밀번호: ");
        // System.out.println(encoder.encode("admin1234")); 

        return new BCryptPasswordEncoder(); // 비밀번호 암호화용 인코더
    }

    // HTTP 보안 설정 - 세션 기반 인증 제어
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (JSP 폼에 CSRF 토큰 안 씀)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()) // 모든 요청 허용 (세션에서 직접 제어함)
                .formLogin(login -> login.disable()); // Spring 기본 로그인 UI 제거

        return http.build();
    }

}
