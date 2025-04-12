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
        return new BCryptPasswordEncoder(); // 비밀번호 암호화용 인코더
    }

    // HTTP 보안 설정 - 세션 기반 인증 제어
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인은 비인증 사용자도 접근 허용
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll()
                        // 마이페이지, 로그아웃은 로그인된 사용자만 접근 가능
                        .requestMatchers("/api/user/me", "/api/user/logout").authenticated()
                        // 그 외 요청은 허용 (원하면 막을 수도 있음)
                        .anyRequest().permitAll())
                // 기본 로그인 폼 비활성화 (REST API 사용 중이므로)
                .formLogin(login -> login.disable());

        return http.build();
    }

}
