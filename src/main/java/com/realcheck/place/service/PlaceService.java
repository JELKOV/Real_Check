package com.realcheck.place.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.place.dto.PlaceDetailsDto;
import com.realcheck.place.dto.PlaceDto;
import com.realcheck.place.entity.AllowedRequestType;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.AllowedRequestTypeRepository;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * PlaceService 클래스
 * - 장소(Place) 관련 비즈니스 로직을 담당하는 서비스 계층
 * - 컨트롤러로부터 요청을 받아 실제 동작을 처리하고, DB 작업은 Repository에 위임함
 */
@Service
@RequiredArgsConstructor
public class PlaceService {
        private final PlaceRepository placeRepository;
        private final UserRepository userRepository;
        private final StatusLogRepository statusLogRepository;
        private final AllowedRequestTypeRepository allowedRequestTypeRepository;

        // ─────────────────────────────────────────────
        // [1] 장소 등록 관련
        // ─────────────────────────────────────────────

        /**
         * [1-1] 장소 등록 메서드 [미사용]
         * - 사용자 ID(ownerId)를 통해 User 조회
         * - PlaceDto → Place Entity 변환 후 저장
         */
        public void registerPlace(PlaceDto dto) {
                // 1. 등록자(ownerId)를 통해 User 객체 조회
                User owner = userRepository.findById(dto.getOwnerId())
                                .orElseThrow(() -> new RuntimeException(
                                                "등록자(User ID: " + dto.getOwnerId() + ")를 찾을 수 없습니다."));
                // 2. DTO → Entity 변환
                Place place = dto.toEntity(owner);
                // 3. DB에 장소 저장
                placeRepository.save(place);
        }

        // ─────────────────────────────────────────────
        // [2] 장소 조회 관련
        // ─────────────────────────────────────────────

        /**
         * [2-1] 전체 장소 목록 조회 [미사용]
         * - DB에서 모든 Place 엔티티를 조회하고, PlaceDto로 변환하여 반환 (관리자 전용)
         */
        public List<PlaceDto> findAll() {
                return placeRepository.findAll().stream()
                                .map(PlaceDto::fromEntity) // 엔티티 → DTO 변환
                                .toList();
        }

        /**
         * [2-2] 특정 사용자가 등록한 장소 조회 [미사용]
         * - 로그인 사용자 본인이 등록한 장소 목록 반환
         */
        public List<PlaceDto> findByOwner(Long ownerId) {
                return placeRepository.findByOwnerId(ownerId).stream()
                                .map(PlaceDto::fromEntity) // 엔티티 → DTO 변환
                                .toList();
        }

        /**
         * [2-3] 현재 위치 기준 반경 내 장소 조회 [미사용]
         * - 사용자의 위도(lat), 경도(lng)를 기반으로 주변 장소 조회
         */
        public List<PlaceDto> findNearbyPlaces(double lat, double lng, double radiusMeters) {
                return placeRepository.findNearbyPlaces(lat, lng, radiusMeters).stream()
                                .map(PlaceDto::fromEntity)
                                .toList();
        }

        /**
         * [2-4] 승인된 장소만 필터링 (사용자 노출용) [미사용] - 장소 검색 페이지
         * - 사용자에게 보여줄 장소를 제한함
         */
        public List<PlaceDto> findApprovedNearby(double lat, double lng, double radiusMeters) {
                return placeRepository.findApprovedNearby(lat, lng, radiusMeters)
                                .stream()
                                .map(PlaceDto::fromEntity)
                                .toList();
        }

        /**
         * [2-5] 검색어 기반 장소 조회 (승인된 장소만)
         * PlaceController: searchApprovedPlaces
         * - 승인된 장소 & 검색어 기반 조회
         */
        public List<PlaceDto> searchApprovedPlaces(String query) {
                return placeRepository.findApprovedByNameContaining(query).stream()
                                .map(PlaceDto::fromEntity)
                                .toList();
        }

        /**
         * [2-6] 장소 상세 정보 조회 (공식 장소)
         * PageController: getPlaceForRegister
         * PageController: showCommunityPage
         * PlaceController: getPlaceDetails
         * - 장소 상세 정보 + 허용된 요청 타입 목록 반환
         */
        public PlaceDetailsDto getPlaceDetails(Long id) {
                Place place = placeRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("해당 장소를 찾을 수 없습니다."));

                // 1. 최신 REGISTER 로그 content 가져오기
                StatusLog latestRegister = statusLogRepository
                                .findTopByPlaceIdAndStatusTypeOrderByCreatedAtDesc(id, StatusType.REGISTER);
                String recentInfo = (latestRegister != null) ? latestRegister.getContent() : null;

                // 2. 허용된 요청 타입
                Set<String> allowedRequestTypes = allowedRequestTypeRepository.findByPlaceId(id).stream()
                                .map(type -> type.getRequestType().name())
                                .collect(Collectors.toSet());

                // 3. 커뮤니티 링크 생성
                String communityLink = "/place/community/" + id;

                // 4. ownerId 가져오기
                Long ownerId = (place.getOwner() != null) ? place.getOwner().getId() : null;

                // 4. DTO 조립 (여기서 직접 생성)
                return new PlaceDetailsDto(
                                place.getId(),
                                place.getName(),
                                place.getAddress(),
                                place.getLat(),
                                place.getLng(),
                                place.isApproved(),
                                ownerId,
                                recentInfo,
                                communityLink,
                                allowedRequestTypes);
        }

        // ─────────────────────────────────────────────
        // [3] 요청 카테고리 등록 및 삭제 (AllowedRequestType)
        // ─────────────────────────────────────────────

        /**
         * [3-1] 특정 장소에 허용된 요청 타입 추가 [미사용]
         * - 지정된 장소에 특정 요청 타입을 추가 (관리자 전용)
         */
        @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
        @Transactional
        public void addAllowedRequestType(Long placeId, String requestType) {
                Place place = placeRepository.findById(placeId)
                                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. ID: " + placeId));

                boolean alreadyExists = allowedRequestTypeRepository.existsByPlaceIdAndRequestType(placeId,
                                RequestCategory.valueOf(requestType));

                if (!alreadyExists) {
                        AllowedRequestType allowedType = new AllowedRequestType(place,
                                        RequestCategory.valueOf(requestType));
                        allowedRequestTypeRepository.save(allowedType);
                }
        }

        /**
         * [3-2] 특정 장소에 허용된 요청 타입 제거 [미사용]
         * - 지정된 장소에서 특정 요청 타입 제거
         */
        public void removeAllowedRequestType(Long placeId, String requestType) {
                allowedRequestTypeRepository.deleteByPlaceIdAndRequestType(placeId,
                                RequestCategory.valueOf(requestType));
        }

}