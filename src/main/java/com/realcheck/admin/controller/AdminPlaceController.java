package com.realcheck.admin.controller;

import com.realcheck.admin.dto.AdminPlaceDetailsDto;
import com.realcheck.admin.dto.AdminPlaceDto;
import com.realcheck.admin.service.AdminPlaceService;
import com.realcheck.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/places")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlaceController {

    private final AdminPlaceService adminPlaceService;

    /**
     * [1] 장소 목록 조회 (페이징, 검색어, 승인 여부 필터)
     * page: admin/place.jsp
     */
    @GetMapping
    public ResponseEntity<Page<AdminPlaceDto>> listPlaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status // "approved", "rejected", "pending"
    ) {
        Page<AdminPlaceDto> result = adminPlaceService.listPlaces(page, size, q, status);
        return ResponseEntity.ok(result);
    }

    /**
     * [2] 장소 상세 조회
     * page: admin/place.jsp
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<AdminPlaceDetailsDto> getPlaceDetails(
            @PathVariable Long placeId) {
        AdminPlaceDetailsDto dto = adminPlaceService.getPlaceDetails(placeId);
        return ResponseEntity.ok(dto);
    }

    /**
     * [3] 장소 승인 처리
     * page: admin/place.jsp
     */
    @PostMapping("/{placeId}/approve")
    public ResponseEntity<Void> approvePlace(
            @PathVariable Long placeId,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 및 관리자 권한 체크
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build(); // 권한 없음
        }

        adminPlaceService.approvePlace(placeId, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * [4] 장소 반려 처리
     * page: admin/place.jsp
     */
    @PostMapping("/{placeId}/reject")
    public ResponseEntity<Void> rejectPlace(
            @PathVariable Long placeId,
            @RequestParam String reason,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인 및 관리자 권한 체크
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return ResponseEntity.status(403).build(); // 권한 없음
        }
        
        adminPlaceService.rejectPlace(placeId, reason, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * [5] 장소 삭제
     * page: admin/place.jsp
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @PathVariable Long placeId) {
        adminPlaceService.deletePlace(placeId);
        return ResponseEntity.noContent().build();
    }
}
