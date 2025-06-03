package com.realcheck.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realcheck.place.dto.FavoritePlaceDto;
import com.realcheck.place.dto.PlaceDetailsDto;
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

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // ─────────────────────────────────────────────
    // [1] 장소 등록
    // ─────────────────────────────────────────────

    /**
     * [1-1] 장소 등록 API (POST /api/place) [미사용]
     * - 프론트엔드에서 장소 정보를 JSON으로 보내면 등록됨
     * - 관리자 또는 인증된 사용자만 가능 (프론트에서 추가 검증 필요)
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
     * [2-1] 전체 장소 목록 조회 API (GET /api/place) [미사용]
     * - 모든 등록된 장소 조회 (관리자 전용)
     * - 예: 관리자 페이지에서 모든 장소 관리
     */
    @GetMapping
    public ResponseEntity<List<PlaceDto>> getAll() {
        return ResponseEntity.ok(placeService.findAll()); // 모든 장소를 서비스에서 조회
    }

    /**
     * [2-2] 내 장소 목록 조회 API (GET /api/place/my) [미사용]
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
     * [2-3] 현재 위치 기반 주변 장소 조회 API
     * page: place/place-search.jsp
     * - 위도(lat), 경도(lng), 반경(radiusMeters)을 기반으로 인근 장소 조회
     * - 지도에서 사용자 위치를 기준으로 인근 장소 표시
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceDto>> getNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") double radiusMeters) {
        return ResponseEntity.ok(placeService.findNearbyPlaces(lat, lng, radiusMeters));
    }

    /**
     * [2-4] 장소 검색 API (검색어 기반)
     * page: request/register.jsp
     * page: place/page-search.jsp
     * - 검색어를 기반으로 승인된 장소 조회
     * - 장소 등록 시 협력 지정 위치의 업체 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<PlaceDto>> searchApprovedPlaces(@RequestParam String query) {
        List<PlaceDto> places = placeService.searchApprovedPlaces(query);
        return ResponseEntity.ok(places);
    }

    /**
     * [2-5] 공식 장소 상세 정보 조회 API
     * page: request/register.jsp
     * - 특정 장소의 상세 정보 조회
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<PlaceDetailsDto> getPlaceDetails(@PathVariable Long id) {
        PlaceDetailsDto details = placeService.getPlaceDetails(id);
        return ResponseEntity.ok(details);
    }

    // ─────────────────────────────────────────────
    // [3] 즐겨 찾기
    // ─────────────────────────────────────────────

    /**
     * [3-1] 즐겨찾기 토글 API (POST /api/place/{placeId}/favorite)
     * page: place/place-search.jsp
     * - 로그인한 사용자가 해당 장소를 즐겨찾기 등록 또는 해제
     * - 이미 등록된 상태면 삭제, 아니면 추가
     */
    @PostMapping("/{placeId}/favorite")
    public ResponseEntity<?> toggleFavoritePlace(@PathVariable Long placeId, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        if (loginUser == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        boolean isFavorite = placeService.toggleFavoritePlace(placeId, loginUser.getId());

        return ResponseEntity.ok(
                isFavorite ? "즐겨찾기 등록 완료" : "즐겨찾기 해제 완료");
    }

    /**
     * [3-2] 즐겨찾기 여부 확인 API (GET /api/place/{placeId}/is-favorite)
     * page: place/place-search.jsp
     * - 현재 로그인한 사용자가 해당 장소를 즐겨찾기 중인지 여부 확인
     * - 즐겨찾기 상태에 따라 버튼 UI를 초기화하기 위해 사용
     * - 프론트에서 장소 선택 시 호출됨
     */
    @GetMapping("/{placeId}/is-favorite")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long placeId, HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 상태 확인
        if (loginUser == null)
            return ResponseEntity.status(401).build();

        // 즐겨찾기 여부 조회
        boolean isFavorite = placeService.isFavorite(placeId, loginUser.getId());
        return ResponseEntity.ok(isFavorite);
    }

    /**
     * [3-3] 즐겨찾기 목록 조회 API (GET /api/place/favorites) [미사용]
     * page: 즐겨찾기 페이지(추후 구현 예정)
     * - 현재 로그인한 사용자가 등록한 즐겨찾기 장소 목록을 조회
     * - FavoritePlace 엔티티 기반으로 FavoritePlaceDto 리스트 반환
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<FavoritePlaceDto>> getMyFavorites(HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 확인
        if (loginUser == null)
            return ResponseEntity.status(401).build();

        // 해당 사용자의 즐겨찾기 장소 리스트 조회
        List<FavoritePlaceDto> favorites = placeService.getFavoritesByUser(loginUser.getId());
        return ResponseEntity.ok(favorites);
    }
}
