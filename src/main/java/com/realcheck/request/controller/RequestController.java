package com.realcheck.request.controller;

import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.entity.Request;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.request.service.RequestService;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * RequestController (ALL DONE)
 * - 요청(Request) 관련 REST API 컨트롤러
 * - 요청 등록, 조회, 반경 기반 필터링 등을 담당
 */
@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final UserService userService;

    // ────────────────────────────────────────
    // [1] 요청 등록
    // ────────────────────────────────────────

    /**
     * [1-1] 요청 등록 API
     * page: request/register.jsp
     * - 세션 로그인 사용자만 가능
     * - RequestDto → Entity 변환은 Service 내부에서 처리
     */
    @PostMapping
    public ResponseEntity<?> createRequest(@Valid @RequestBody RequestDto dto, HttpSession session) {
        UserDto loginUserDto = (UserDto) session.getAttribute("loginUser");
        if (loginUserDto == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // UserDto를 User로 변환
        User loginUser = userService.convertToUser(loginUserDto);

        Request savedRequest = requestService.createRequest(dto, loginUser);
        // 생성 직후에는 statusLog가 없으므로 visibleCount = 0 명시적으로 전달
        return ResponseEntity.ok(RequestDto.fromEntity(savedRequest, 0));
    }

    // ────────────────────────────────────────
    // [2] 요청 조회
    // ────────────────────────────────────────

    /**
     * [2-1] 지역 기반 요청이 3시간이 지나서 오픈된 요청 조회 API
     * page: request/list.jsp
     * - 미마감 요청
     * - 답변 3개 미만
     * - 3시간 지난 요청
     */
    @GetMapping("/open")
    public ResponseEntity<List<RequestDto>> findOpenRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius,
            @RequestParam(required = false) String category) {

        List<RequestDto> dtoList = requestService.findOpenRequests(page, size, lat, lng, radius, category);
        return ResponseEntity.ok(dtoList);
    }

    /**
     * [2-2] 최신 요청 조회 (현재 사용자 위치 기준 / 지도 반경 기반) API
     * page: map/requset-list.jsp
     * - 위도, 경도 기준으로 radius(m) 이내
     * - 장소 좌표가 존재하며 답변 수가 3개 미만인 요청 필터
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<RequestDto>> findNearbyOpenRequests(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3000") double radiusMeters) {

        List<RequestDto> dtoList = requestService.findNearbyValidRequests(lat, lng, radiusMeters);
        return ResponseEntity.ok(dtoList);
    }

    /**
     * [2-3] 요청 단건 상세 조회 (ID로 조회) API
     * page: request/detail.jsp
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findRequestById(@PathVariable Long id) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("요청을 찾을 수 없습니다.");
        }
        Request r = requestOpt.get();
        int visibleCount = requestService.countVisibleStatusLogsByRequestId(r.getId());
        return ResponseEntity.ok(RequestDto.fromEntity(r, visibleCount));
    }

    /**
     * [2-4] 특정 사용자(userId)의 요청 목록 조회 API
     * - page: request/my-requests.jsp
     * - 로그인한 사용자의 요청 목록을 조회하며, 카테고리/검색어/페이지네이션 기능 제공
     */
    @GetMapping("/my")
    public ResponseEntity<?> findMyRequests(
            @RequestParam(required = false) RequestCategory category,
            @RequestParam(required = false) String keyword, // 키워드 필터 (nullable)
            @RequestParam(defaultValue = "0") int page, // 현재 페이지 번호
            @RequestParam(defaultValue = "10") int size, // 페이지 당 항목 수
            HttpSession session // 로그인 사용자 세션
    ) {
        // [1] 세션에서 로그인 사용자 정보 가져오기
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인 정보가 없으면 401 Unauthorized 응답
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // [2] 요청 서비스 호출: 카테고리 + 키워드 + 페이지네이션 기반 요청 목록 조회
        Page<RequestDto> result = requestService.findMyRequests(
                loginUser.getId(), category, keyword, page, size);

        // [3] 결과 반환
        return ResponseEntity.ok(result);
    }

    /**
     * [2-5] 요청 카테고리 목록 조회 API
     * page: request/my-requests.jsp (카테고리 필터용)
     * - 클라이언트에서 <select> 옵션으로 사용
     */
    @GetMapping("/categories")
    public ResponseEntity<List<RequestCategory>> getAllCategories() {
        return ResponseEntity.ok(List.of(RequestCategory.values()));
    }

    // ────────────────────────────────────────
    // [3] 요청 마감
    // ────────────────────────────────────────

    /**
     * [3-1] 요청자 요청취소 (스스로 마감처리) API
     * page: request/detail.jsp
     * - 사용자가 질문이 한개도 안달린 상태에서 직접 취소 처리
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<?> closeRequest(
            @PathVariable Long id,
            @RequestParam Long userId) {
        requestService.closeRequest(id, userId);
        return ResponseEntity.ok("요청이 수동으로 마감되었습니다.");
    }

}
