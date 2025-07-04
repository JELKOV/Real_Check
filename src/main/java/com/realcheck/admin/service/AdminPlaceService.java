package com.realcheck.admin.service;

import com.realcheck.admin.dto.AdminPlaceDetailsDto;
import com.realcheck.admin.dto.AdminPlaceDto;
import com.realcheck.admin.entity.ActionType;
import com.realcheck.admin.entity.TargetType;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.place.repository.AllowedRequestTypeRepository;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.place.repository.FavoritePlaceRepository;
import com.realcheck.request.repository.RequestRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPlaceService {

    private final PlaceRepository placeRepository;
    private final AllowedRequestTypeRepository allowedRequestTypeRepository;
    private final StatusLogRepository statusLogRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final RequestRepository requestRepository;
    private final AdminActionLogService adminActionLogService;

    /**
     * [1] 장소 목록 조회 (페이징, 검색어, 승인 여부 필터)
     * AdminPlaceController: listPlaces
     */
    public Page<AdminPlaceDto> listPlaces(int page, int size, String q, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Place> places;

        boolean hasQ = (q != null && !q.isBlank());
        String st = (status == null || status.isBlank()) ? "ALL" : status.toUpperCase();

        if (hasQ) {
            // 검색어가 있을 때
            switch (st) {
                case "APPROVED":
                    places = placeRepository
                            .findByIsApprovedAndNameContainingIgnoreCase(true, q, pageable);
                    break;
                case "REJECTED":
                    places = placeRepository
                            .findByIsRejectedAndNameContainingIgnoreCase(true, q, pageable);
                    break;
                case "PENDING":
                    places = placeRepository
                            .findByIsApprovedAndIsRejectedAndNameContainingIgnoreCase(false, false, q, pageable);
                case "ALL":
                default:
                    places = placeRepository.findByNameContainingIgnoreCase(q, pageable);
                    break;
            }
        } else {
            // 검색어 없을 때 (기존 상태 분기)
            switch (st) {
                case "APPROVED":
                    places = placeRepository.findByIsApproved(true, pageable);
                    break;
                case "REJECTED":
                    places = placeRepository.findByIsRejected(true, pageable);
                    break;
                case "PENDING":
                    places = placeRepository.findByIsApprovedAndIsRejected(false, false, pageable);
                    break;
                case "ALL":
                default:
                    places = placeRepository.findAll(pageable);
                    break;
            }
        }

        return places.map(place -> {
            String ownerName = Optional.ofNullable(place.getOwner())
                    .map(User::getNickname)
                    .orElse(null);
            return AdminPlaceDto.fromEntity(place, ownerName);
        });
    }

    /**
     * [2] 장소 상세 조회
     * AdminPlaceController: getPlaceDetails
     */
    @Transactional(readOnly = true)
    public AdminPlaceDetailsDto getPlaceDetails(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found: " + placeId));

        // 2-1) ownerName
        User owner = place.getOwner();
        String ownerName = (owner != null) ? owner.getNickname() : null;

        // 2-2) 허용된 요청 타입
        Set<String> allowedTypes = allowedRequestTypeRepository.findByPlaceId(placeId).stream()
                .map(art -> art.getRequestType().name())
                .collect(Collectors.toSet());

        // 2-3) 최신 REGISTER 공지
        StatusLog latestRegister = statusLogRepository
                .findTopByPlaceIdAndStatusTypeOrderByCreatedAtDesc(placeId, StatusType.REGISTER);
        String recentInfo = (latestRegister != null) ? latestRegister.getContent() : null;

        // 2-4) 커뮤니티 링크
        String communityLink = "/place/community/" + placeId;

        // 2-5) optional 통계들
        long favoriteCount = favoritePlaceRepository.countByPlaceId(placeId);
        long requestCount = requestRepository.countByPlaceId(placeId);
        long statusLogCount = statusLogRepository.countByPlaceId(placeId);

        return AdminPlaceDetailsDto.of(
                place,
                ownerName,
                allowedTypes,
                recentInfo,
                communityLink,
                favoriteCount,
                requestCount,
                statusLogCount);
    }

    /**
     * [3] 장소 승인 처리
     * AdminPlaceController: approvePlace
     */
    @Transactional
    public void approvePlace(Long placeId, Long adminId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found: " + placeId));
        place.setApproved(true);
        place.setRejected(false);

        placeRepository.save(place);

        adminActionLogService.saveLog(
                adminId,
                place.getId(),
                ActionType.APPROVE,
                TargetType.PLACE,
                "장소 승인 처리 완료");
    }

    /**
     * [4] 장소 반려 처리
     * AdminPlaceController: rejectPlace
     */
    @Transactional
    public void rejectPlace(Long placeId, String reason, Long adminId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found: " + placeId));
        place.setApproved(false);
        place.setRejected(true);
        place.setRejectReason(reason); // 사유 저장

        placeRepository.save(place);

        // Admin 로그 기록
        adminActionLogService.saveLog(
                adminId,
                place.getId(),
                ActionType.REJECT,
                TargetType.PLACE,
                "장소 반려 처리: " + reason);
    }

    /**
     * [5] 장소 삭제
     * AdminPlaceController: deletePlace
     */
    @Transactional
    public void deletePlace(Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new EntityNotFoundException("Place not found: " + placeId);
        }
        placeRepository.deleteById(placeId);
    }
}
