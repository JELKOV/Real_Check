package com.realcheck.request.controller;

import com.realcheck.request.entity.Request;
import com.realcheck.request.service.RequestService;
import com.realcheck.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // 요청 등록
    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody Request request, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        request.setUser(loginUser); // 요청자 설정
        Request savedRequest = requestService.createRequest(request);
        return ResponseEntity.ok(savedRequest);
    }

    // 전체 미마감 요청 조회 (주변 요청 확인용)
    @GetMapping("/open")
    public ResponseEntity<List<Request>> findOpenRequests() {
        List<Request> openRequests = requestService.findOpenRequests();
        return ResponseEntity.ok(openRequests);
    }

    // 특정 요청 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> findRequestById(@PathVariable Long id) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("요청을 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(requestOpt.get());
    }

}
