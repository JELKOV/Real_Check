package com.realcheck.status.controller;

import com.realcheck.common.dto.PageResult;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.service.UserService;

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
    private final UserService userService;

    // ─────────────────────────────────────────────
    // [1] 사용자 기능 - 질문 없는 정보 상태 로그 등록 (CREATE)
    // ─────────────────────────────────────────────

    /**
     * [1-1] 공식 장소 상태 등록 API (REGISTER)
     * page: place/register.jsp
     * - 장소 소유자나 관리자가 직접 상태 등록
     */
    @PostMapping("/register-public")
    public ResponseEntity<String> register(@RequestBody StatusLogDto dto, HttpSession session) {
        UserDto loginUserDto = (UserDto) session.getAttribute("loginUser");
        if (loginUserDto == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다");

        // UserDto → User 변환 (유효성 검사)
        User loginUser = userService.convertToUser(loginUserDto);

        statusLogService.register(loginUser.getId(), dto);
        return ResponseEntity.ok("등록 완료");
    }

    /**
     * [1-2] 자발적 공유 등록 API (FREE_SHARE) [미완성]
     * - 로그인한 사용자가 장소 상태를 자유롭게 공유
     * - StatusLog 타입: FREE_SHARE
     */
    @PostMapping("/free-share")
    public ResponseEntity<?> registerFreeShare(@RequestBody StatusLogDto dto, HttpSession session) {
        UserDto loginUserDto = (UserDto) session.getAttribute("loginUser");
        if (loginUserDto == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // UserDto → User 변환 (유효성 검사)
        User loginUser = userService.convertToUser(loginUserDto);

        statusLogService.registerFreeShare(loginUser.getId(), dto);
        return ResponseEntity.ok("자발적 공유 등록 완료");
    }

    // ─────────────────────────────────────────────
    // [2] 사용자 기능 - 상태 로그 조회 (READ)
    // ─────────────────────────────────────────────

    /**
     * [2-1] 내가 등록한 상태 로그 목록 조회
     * page: status/my-logs.jsp
     * - 세션 사용자 ID 기반으로 내 기록만 조회
     */
    @GetMapping("/my")
    public ResponseEntity<PageResult<StatusLogDto>> getMyStatusLogs(
            HttpSession session,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type, // "ANSWER", "FREE_SHARE", ...
            @RequestParam(defaultValue = "false") boolean hideHidden // true면 숨김 제외
    ) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        PageResult<StatusLogDto> result = statusLogService.getLogsByUser(
                loginUser.getId(), page, size, type, hideHidden);
        return ResponseEntity.ok(result);
    }

    /**
     * [2-2] 요청 ID로 연결된 답변 목록 조회 API
     * - page: request/detail.jsp
     */
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<List<StatusLogDto>> getAnswersByRequest(@PathVariable Long requestId) {
        List<StatusLogDto> answers = statusLogService.getAnswersByRequestId(requestId);
        return ResponseEntity.ok(answers);
    }

    /**
     * [2-3] 현재 위치 기반 근처 상태 로그 조회 API [CHECK]
     * page: map/nearby.jsp
     * - 위도, 경도, 반경(m)을 기준으로 3시간 이내 상태 로그 조회
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<StatusLogDto>> getNearbyStatusLogs(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") double radiusMeters) {

        List<StatusLogDto> logs = statusLogService.findNearbyStatusLogs(lat, lng, radiusMeters);
        return ResponseEntity.ok(logs);
    }

    // ─────────────────────────────────────────────
    // [3] 사용자 기능 - 상태 로그 수정 (UPDATE)
    // ─────────────────────────────────────────────

    /**
     * [3-1] 상태 로그 수정 API
     * page: request/detail.jsp
     * page: request/my-logs.jsp
     * page: place/edit.jsp
     * - 로그인한 사용자가 본인이 작성한 상태 로그를 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStatusLog(
            @PathVariable Long id,
            @RequestBody StatusLogDto dto,
            HttpSession session) {
        UserDto loginUserDto = (UserDto) session.getAttribute("loginUser");
        if (loginUserDto == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }

        try {
            // UserDto → User 변환 (전체 정보 조회)
            User loginUser = userService.convertToUser(loginUserDto);
            statusLogService.updateStatusLog(id, loginUser.getId(), dto);
            return ResponseEntity.ok("수정 완료");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * [3-2] 요청 기반 답변 채택 API
     * page: request/detail.jsp
     * - 요청 작성자만 자신이 받은 답변 중 하나를 채택 가능
     * - 해당 StatusLog에 isSelected = true, 요청 마감 처리
     */
    @PostMapping("/select/{statusLogId}")
    public ResponseEntity<?> selectAnswer(@PathVariable Long statusLogId, HttpSession session) {
        UserDto loginUserDto = (UserDto) session.getAttribute("loginUser");
        if (loginUserDto == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            // UserDto → User 변환 (전체 정보 조회)
            User loginUser = userService.convertToUser(loginUserDto);
            statusLogService.selectAnswer(statusLogId, loginUser.getId());
            return ResponseEntity.ok("답변이 채택되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // [4] 사용자 기능 - 상태 로그 삭제 (DELETE)
    // ─────────────────────────────────────────────

    /**
     * [4-1] 상태 로그 삭제 API
     * page: request/detail.jsp
     * page: request/my-logs.jsp
     * page: place/community.jsp
     * - 로그인한 사용자가 본인이 작성한 로그만 삭제 가능
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStatusLog(
            @PathVariable Long id,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            statusLogService.deleteStatusLog(id, loginUser.getId());
            return ResponseEntity.ok("삭제 완료");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // ────────────────────────────────────────
    // [5] 관리자 기능
    // ────────────────────────────────────────

    /**
     * [5-1] 관리자 전용 전체 StatusLog 목록 조회 API [미사용]
     * 조건: 로그인한 사용자가 관리자일 경우에만 전체 로그 반환
     * 반환: 모든 상태 로그 리스트 (StatusLogDto)
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