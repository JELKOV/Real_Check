package com.realcheck.request.controller;

import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.entity.Request;
import com.realcheck.request.service.RequestService;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
     * page: request/register.jsp
     * [1] 요청 등록 API 
     * - 세션 로그인 사용자만 가능
     * - RequestDto → Entity 변환은 Service 내부에서 처리
     */
    @PostMapping
    public ResponseEntity<?> createRequest(@Valid @RequestBody RequestDto dto, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Request savedRequest = requestService.createRequest(dto, loginUser);
        return ResponseEntity.ok(RequestDto.fromEntity(savedRequest));
    }

    /**
     * page: request/list.jsp
     * [2] 미마감 + 답변 3개 미만 요청 조회
     * - 리스트 조회용
     */
    @GetMapping("/open")
    public ResponseEntity<List<RequestDto>> findOpenRequests() {
        List<Request> entities = requestService.findOpenRequests();
        List<RequestDto> dtoList = entities.stream()
                .map(RequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    /**
     * page: map/requset-list.jsp
     * [3] 반경 내 요청 조회 (지도용)
     * - 마감되지 않고 답변 3개 미만이며 위도/경도 존재하는 요청
     * - 3시간 이내의 요청만 조회
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<RequestDto>> findNearbyOpenRequests(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3000") double radiusMeters) {

        List<Request> entities = requestService.findNearbyValidRequests(lat, lng, radiusMeters);
        List<RequestDto> dtoList = entities.stream()
                .map(RequestDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /**
     * page: request/detail.jsp
     * [4] 특정 요청 상세 조회
     * - URL: GET /api/request/{id}
     * - 요청 ID를 기반으로 요청 객체 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findRequestById(@PathVariable Long id) {
        Optional<Request> requestOpt = requestService.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("요청을 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(RequestDto.fromEntity(requestOpt.get()));
    }

    /**
     * page: request/my-requests.jsp
     * [5] 로그인한 사용자의 요청 목록 조회
     * - 세션에서 사용자 ID를 확인 후 해당 사용자의 요청 반환
     */
    @GetMapping("/my")
    public ResponseEntity<?> findMyRequests(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        List<Request> myRequests = requestService.findByUserId(loginUser.getId());
        List<RequestDto> dtoList = myRequests.stream()
                .map(RequestDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

}
