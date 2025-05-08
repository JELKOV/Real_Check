package com.realcheck.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realcheck.place.dto.PlaceDto;
import com.realcheck.place.service.PlaceService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * PlaceController 클래스
 * - 장소(Place)와 관련된 요청을 처리하는 컨트롤러 클래스
 * - 사용자의 요청을 받아 PlaceService를 통해 비즈니스 로직을 처리
 */

@RestController // 이 클래스는 REST API를 처리하는 컨트롤러라는 것을 Spring에게 알려줌 (응답은 JSON 형식으로 반환됨)
@RequestMapping("/api/place") // 이 컨트롤러의 모든 API는 "/api/place"로 시작하는 경로를 가짐
@RequiredArgsConstructor // final로 선언된 필드(PlaceService)를 자동으로 생성자 주입해줌 (의존성 주입)
public class PlaceController {
    // PlaceService는 실제 장소 등록/조회 등 비즈니스 로직을 처리하는 서비스 클래스
    private final PlaceService placeService;

    // ─────────────────────────────────────────────
    // [1] 장소 등록
    // ─────────────────────────────────────────────

    /**
     * [1] 장소 등록 API (POST /api/place)
     * - 프론트엔드에서 장소 정보를 JSON으로 보내면 등록됨
     */
    @PostMapping
    public ResponseEntity<String> register(@Valid @RequestBody PlaceDto dto) {
        placeService.registerPlace(dto);
        return ResponseEntity.ok("장소 등록 완료");
    }

    // ─────────────────────────────────────────────
    // [2] 장소 조회
    // ─────────────────────────────────────────────

    /**
     * [1] 전체 장소 목록 조회 API (GET /api/place)
     * - 모든 등록된 장소 조회
     * - 관리자 전용
     */
    @GetMapping
    public ResponseEntity<List<PlaceDto>> getAll() {
        return ResponseEntity.ok(placeService.findAll()); // 모든 장소를 서비스에서 조회
    }

    /**
     * [2] 내 장소 목록 조회 API (GET /api/place/my)
     * - 로그인된 사용자가 등록한 장소만 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<PlaceDto>> getMyPlaces(HttpSession session) {
        // 세션에서 로그인된 사용자 정보 꺼내기
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 상태가 아니면 401 Unauthorized 반환
        if (loginUser == null)
            return ResponseEntity.status(401).build();
        // 로그인된 사용자의 ID로 등록된 장소만 조회하여 반환
        return ResponseEntity.ok(placeService.findByOwner(loginUser.getId()));
    }

    /**
     * [3] 현재 위치 기반 주변 장소 조회 API
     * - 위도, 경도, 반경을 기반으로 인근 장소 필터링
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceDto>> getNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") double radiusMeters) {
        return ResponseEntity.ok(placeService.findNearbyPlaces(lat, lng, radiusMeters));
    }

    /**
     * page: request/register.jsp
     * [4] 장소 검색 API (검색어 기반)
     * - 등록시 협력 지정위치의 업체 조회
     */
    @GetMapping("/search")
    public ResponseEntity<List<PlaceDto>> searchApprovedPlaces(@RequestParam String query) {
        List<PlaceDto> places = placeService.searchApprovedPlaces(query);
        return ResponseEntity.ok(places);
    }
}
