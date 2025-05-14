package com.realcheck.status.controller;

import com.realcheck.request.entity.Request;
import com.realcheck.request.service.RequestService;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.dto.UserDto;
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

    /**
     * page: request/detail.jsp
     * [1] 요청에 대한 답변 등록 → StatusLog(type: ANSWER)에 저장
     * - 사용자가 요청(Request)에 대해 답변(StatusLog)을 등록하는 컨트롤러
     */
    @PostMapping("/{requestId}")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long requestId,
            @RequestBody StatusLogDto dto,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        System.out.println(">> 세션 loginUser: " + loginUser);
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Optional<Request> requestOpt = requestService.findById(requestId);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("요청을 찾을 수 없습니다.");
        }

        Request request = requestOpt.get();

        // 마감된 요청은 답변 등록 불가
        if (request.isClosed()) {
            return ResponseEntity.status(403).body("이미 마감된 요청입니다.");
        }

        // 최대 답변 수 확인 (3개)
        if (request.getStatusLogs().size() >= 3) {
            return ResponseEntity.status(403).body("이미 최대 답변 수(3개)에 도달했습니다.");
        }

        // 답변 중복 등록 방지 (서비스 계층에서 검사)
        if (statusLogService.hasUserAnswered(requestId, loginUser.getId())) {
            return ResponseEntity.badRequest().body("이미 답변을 등록하셨습니다.");
        }

        // ANSWER 타입 지정
        dto.setType(StatusType.ANSWER);

        // 연결된 요청 ID 설정
        dto.setRequestId(requestId);

        // 답변 등록 (StatusLogService로 처리)
        statusLogService.registerAnswer(loginUser.getId(), dto);

        return ResponseEntity.ok("답변 등록 완료");
    }
}
