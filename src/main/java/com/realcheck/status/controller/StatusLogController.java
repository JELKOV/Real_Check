package com.realcheck.status.controller;

import com.realcheck.common.dto.PageResult;
import com.realcheck.status.dto.PlaceLogGroupDto;
import com.realcheck.status.dto.StatusLogDto;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.service.StatusLogService;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * StatusLogController (ALL DONE)
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
     * [1-2] 자발적 공유 등록 API (FREE_SHARE)
     * page: place/free-share.jsp
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

    /**
     * [1-3] 자발적 공유 상태 로그 상세 조회 API
     * page: place/free-share-view.jsp
     * - 자발적 공유 상태 로그의 상세 정보 조회
     * - 요청 ID로 조회하며, 해당 ID는 자발적 공유 상태 로그의 ID
     */
    @GetMapping("/free-share/view/{id}")
    public StatusLogDto viewFreeShare(@PathVariable Long id, @SessionAttribute("loginUser") UserDto user) {
        return statusLogService.viewFreeShare(id, user.getId());
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
     * [2-3] 현재 위치 기반 grouped 상태 로그 조회 API
     * page: map/nearby.jsp
     * - REGISTER(공지): 장소당 1개
     * - ANSWER: 요청 응답 전부 포함
     * - placeId 기준으로 묶어서 반환
     */
    @GetMapping("/nearby/grouped")
    public ResponseEntity<List<PlaceLogGroupDto>> getGroupedLogs(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3000") double radiusMeters) {

        List<PlaceLogGroupDto> result = statusLogService.findNearbyGroupedPlaceLogs(lat, lng, radiusMeters);
        return ResponseEntity.ok(result);
    }

    /**
     * [2-4] 일반 장소 위치 응답 로그 조회 API
     * page: map/nearby.jsp
     * - 사용자 요청 응답 중 Place가 없는 것만 조회
     */
    @GetMapping("/nearby/user-locations")
    public ResponseEntity<Page<StatusLogDto>> getNearbyUserLocationLogs(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3000") double radiusMeters,
            @PageableDefault(size = 6, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<StatusLog> logs = statusLogService.findNearbyUserLocationLogs(lat, lng, radiusMeters, pageable);
        return ResponseEntity.ok(logs.map(StatusLogDto::fromEntity));
    }

    /**
     * [2-4] 자발적 공유 상태 로그 조회 API
     * page: map/free-share.jsp
     * - 현재 위치를 기준으로 반경 내 자발적 공유 상태 로그 조회
     * - 위도(lat), 경도(lng), 반경(radiusMeters) 파라미터 사용
     * - 기본 반경: 3000m, 기본 조회 기간: 7일
     */
    @GetMapping("/free-share")
    public PageResult<StatusLogDto> getNearbyFreeShareLogs(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3000") double radiusMeters,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return statusLogService.findNearbyFreeShareLogs(lat, lng, radiusMeters, cutoff, page, size);
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

}