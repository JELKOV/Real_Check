package com.realcheck.request.controller;

import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.entity.Request;
import com.realcheck.request.service.RequestService;
import com.realcheck.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * RequestController
 * - 요청(Request) 관련 REST API 컨트롤러
 * - 요청 등록, 조회, 반경 기반 필터링 등을 담당
 */
@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    /**
     * [1] 요청 등록 API
     * - URL: POST /api/request
     * - 조건: 세션 로그인 사용자만 가능
     * - 요청 본문: JSON(Request)
     * - 응답: 저장된 요청 객체 반환
     *
     * @param request 요청 등록 데이터
     * @param session 현재 세션 (로그인 확인용)
     * @return 저장된 요청 or 401 Unauthorized
     */
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

    /**
     * [2] 미마감 요청 중 답변 부족 요청 조회
     * - URL: GET /api/request/open
     * - 조건: isClosed = false AND 답변 수 < 3
     * - 용도: 홈화면 등에서 전체 오픈 요청 조회
     *
     * @return 조건을 만족하는 요청 리스트
     */
    @GetMapping("/open")
    public ResponseEntity<List<Request>> findOpenRequests() {
        List<Request> openRequests = requestService.findOpenRequests();
        return ResponseEntity.ok(openRequests);
    }

    /**
     * [3] 위치 기반 주변 요청 조회 API
     * - URL: GET /api/request/nearby?lat=...&lng=...&radius=...
     * - 조건:
     * - 반경 radiusMeters 이내
     * - 마감되지 않은 요청
     * - 답변 수가 3개 미만
     * - 용도: 지도에서 내 위치 기준 요청 보기
     *
     * @param lat          위도
     * @param lng          경도
     * @param radiusMeters 거리 반경 (기본값: 3000m)
     * @return 조건을 만족하는 요청 리스트
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<RequestDto>> findNearbyOpenRequests(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3000") double radiusMeters) {

        List<Request> entities = requestService.findNearbyWithFewAnswers(lat, lng, radiusMeters);
        List<RequestDto> dtoList = entities.stream()
                .map(RequestDto::fromEntity)
                .toList(); // ← 엔티티 → DTO 변환

        return ResponseEntity.ok(dtoList);
    }

    /**
     * [4] 특정 요청 상세 조회
     * - URL: GET /api/request/{id}
     * - 요청 ID를 기반으로 요청 객체 조회
     *
     * @param id 요청 ID
     * @return 요청 정보 or 400 Bad Request
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findRequestById(@PathVariable Long id) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("요청을 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(requestOpt.get());
    }

}
