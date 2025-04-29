package com.realcheck.status.controller;

import com.realcheck.request.entity.Request;
import com.realcheck.request.service.RequestService;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final RequestService requestService;
    private final StatusLogService statusLogService;

    // 요청에 대한 답변 등록 → StatusLog(type: ANSWER)에 저장
    @PostMapping("/{requestId}")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long requestId,
            @RequestBody StatusLogDto dto,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Optional<Request> requestOpt = requestService.findById(requestId);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("요청을 찾을 수 없습니다.");
        }

        dto.setType(StatusType.ANSWER); // ANSWER 타입 지정
        dto.setRequestId(requestId);    // 연결된 요청 ID 설정
        statusLogService.registerAnswer(loginUser.getId(), dto);

        return ResponseEntity.ok("답변 등록 완료");
    }
}
