package com.realcheck.controller;

// Spring Web에서 API 만들 때 사용하는 어노테이션
import org.springframework.web.bind.annotation.*;
// User Class 사용
import com.realcheck.entity.User;

@RestController // JSON 형식으로 응답을 반환하는 컨트롤러 클래스
@RequestMapping("/api/user") // 메서드 경로의 시작 부분 / 경로 앞에 /api/user가 붙음
public class UserController {

    @GetMapping("/test") // /api/user/test 요청이 들어오면 이 메서드가 실행됨
    public User testUser() {
        return new User(1L, "test@realcheck.com", "테스트 유저");
    }
}
