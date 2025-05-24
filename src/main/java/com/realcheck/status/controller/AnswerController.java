package com.realcheck.status.controller;

import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AnswerController (ALL DONE)
 * - 사용자가 특정 요청(Request)에 대해 답변(StatusLog)을 등록할 수 있도록 지원하는 API 컨트롤러
 * - 답변은 StatusType.ANSWER로 등록됨
 */
@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final StatusLogService statusLogService;
    private final UserService userService;

    /**
     * [1] 요청에 대한 답변 등록 → StatusLog(type: ANSWER)에 저장
     * page: request/detail.jsp
     * - 사용자가 요청(Request)에 대해 답변(StatusLog)을 등록하는 컨트롤러
     */
    @PostMapping("/{requestId}")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long requestId,
            @RequestBody StatusLogDto dto,
            HttpSession session) {
        UserDto loginUserDto = (UserDto) session.getAttribute("loginUser");
        if (loginUserDto == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            // UserDto → User 변환 (유효성 검사)
            User loginUser = userService.convertToUser(loginUserDto);
            
            // 서비스 계층에서 유효성 검사 및 등록 처리
            statusLogService.registerAnswer(loginUser.getId(), dto, requestId);
            return ResponseEntity.ok("답변 등록 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 요청: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body("요청 처리 불가: " + e.getMessage());
        } catch (RuntimeException e) {
            // 예외 상세 정보 서버 콘솔에 로깅
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: 답변 등록 중 문제가 발생했습니다. " + e.getMessage());
        }
    }

}
