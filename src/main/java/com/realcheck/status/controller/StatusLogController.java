package com.realcheck.status.controller;

import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * StatusLogController
 * - HTTP 요청을 받아 Service 계층에 전달하고 결과를 응답하는 API 컨트롤러
 */
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusLogController {

    private final StatusLogService statusLogService;

    // ────────────────────────────────────────
    // [1] 사용자 기능
    // ────────────────────────────────────────

    /**
     * [1-1] 대기 현황 등록 API
     * - 로그인된 사용자만 가능
     * - 세션에서 사용자 정보를 가져와 등록 처리
     */
    @PostMapping
    public ResponseEntity<String> register(@RequestBody StatusLogDto dto, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다");

        statusLogService.register(loginUser.getId(), dto);
        return ResponseEntity.ok("등록 완료");
    }

    /**
     * [1-2] 장소별 대기 현황 조회 API
     * - 해당 장소 ID로 3시간 이내의 최신 상태 목록 조회
     */
    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<StatusLogDto>> getByPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(statusLogService.getLogsByPlace(placeId));
    }

    /**
     * [1-3] 내가 등록한 상태 로그 목록 조회 API
     * - 세션 사용자 ID 기반으로 내 기록만 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<StatusLogDto>> getMyStatusLogs(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).build(); // 로그인 안됨
        }

        return ResponseEntity.ok(statusLogService.getLogsByUser(loginUser.getId()));
    }

    /**
     * [1-4] 상태 로그 수정 API
     * - 로그인한 사용자가 본인이 작성한 상태 로그를 수정
     * 
     * @param id      수정할 상태 로그 ID
     * @param dto     수정할 데이터 (내용, 대기 인원, 이미지)
     * @param session 현재 로그인한 사용자 세션
     * @return 수정 결과 메시지
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStatusLog(@PathVariable Long id, @RequestBody StatusLogDto dto,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }

        try {
            statusLogService.updateStatusLog(id, loginUser.getId(), dto);
            return ResponseEntity.ok("수정 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    /**
     * [1-5] 상태 로그 삭제 API
     * - 로그인한 사용자가 본인이 작성한 로그만 삭제 가능
     * 
     * @param id      삭제할 상태 로그 ID
     * @param session 현재 로그인한 사용자 세션
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStatusLog(@PathVariable Long id,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            statusLogService.deleteStatusLog(id, loginUser.getId());
            return ResponseEntity.ok("삭제 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ────────────────────────────────────────
    // [2] 관리자 기능
    // ────────────────────────────────────────

    /**
     * [2-1] 관리자 전용 전체 StatusLog 목록 조회 API
     * - URL: GET /api/status/all
     * - 조건: 로그인한 사용자가 관리자일 경우에만 전체 로그 반환
     * - 반환: 모든 상태 로그 리스트 (StatusLogDto)
     */
    @GetMapping("/all")
    public ResponseEntity<List<StatusLogDto>> getAllLogs(HttpSession session) {

        // 현재 로그인한 사용자 정보를 세션에서 꺼냄
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 여부 확인
        // 세션에 로그인 정보가 없으면 401 Unauthorized 반환
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        // 권한 확인 (관리자인지 여부)
        // 로그인한 사용자의 역할이 "ADMIN"이 아니라면 403 Forbidden 반환
        if (!"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build();
        }

        // 전체 로그 조회
        // 서비스 계층에서 전체 StatusLog를 가져옴 (Entity → DTO 변환)
        List<StatusLogDto> logs = statusLogService.getAllLogs();

        // 결과 반환 (200 OK)
        return ResponseEntity.ok(logs);
    }

}