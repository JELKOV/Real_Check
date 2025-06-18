package com.realcheck.place.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.place.dto.FavoritePlaceDto;
import com.realcheck.place.dto.PlaceDetailsDto;
import com.realcheck.place.dto.PlaceDto;
import com.realcheck.place.dto.PlaceRegisterRequestDto;
import com.realcheck.place.entity.AllowedRequestType;
import com.realcheck.place.entity.FavoritePlace;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.AllowedRequestTypeRepository;
import com.realcheck.place.repository.FavoritePlaceRepository;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.request.entity.RequestCategory;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * PlaceService 클래스 (ALL DONE)
 * - 장소(Place) 관련 비즈니스 로직을 담당하는 서비스 계층
 * - 컨트롤러로부터 요청을 받아 실제 동작을 처리하고, DB 작업은 Repository에 위임함
 */
@Service
@RequiredArgsConstructor
public class PlaceService {
        private final PlaceRepository placeRepository;
        private final UserRepository userRepository;
        private final StatusLogRepository statusLogRepository;
        private final FavoritePlaceRepository favoritePlaceRepository;
        private final AllowedRequestTypeRepository allowedRequestTypeRepository;

        // ─────────────────────────────────────────────
        // [1] 장소 등록 관련
        // ─────────────────────────────────────────────
        /**
         * [1-1] 사용자 장소 등록 메서드
         * PlaceController: register
         * - 사용자 ID(ownerId)를 통해 User 조회
         * - PlaceRegisterRequestDto → Place Entity 변환 후 저장
         * - 허용 카테고리 → AllowedRequestType으로 변환해 함께 저장
         */
        @Transactional
        public void registerPlace(PlaceRegisterRequestDto dto, Long ownerId) {
                // 1. 등록자 조회
                User owner = userRepository.findById(ownerId)
                                .orElseThrow(() -> new RuntimeException("등록자(User ID: " + ownerId + ")를 찾을 수 없습니다."));

                // 2. 엔티티 생성
                Place place = Place.builder()
                                .name(dto.getName())
                                .address(dto.getAddress())
                                .lat(dto.getLat())
                                .lng(dto.getLng())
                                .owner(owner)
                                .build();

                // 3. 카테고리 → AllowedRequestType 생성 및 연결 (양방향 연결 필수)
                Set<AllowedRequestType> allowedTypes = dto.getCategories().stream()
                                .map(typeStr -> {
                                        RequestCategory category = RequestCategory.valueOf(typeStr);
                                        return new AllowedRequestType(place, category); // place를 설정한 상태
                                })
                                .collect(Collectors.toSet());

                // 4. 양방향 연관관계 연결
                place.getAllowedRequestTypes().addAll(allowedTypes);

                // 5. place 저장 시 AllowedRequestType도 함께 저장됨 (Cascade.ALL)
                placeRepository.save(place);
        }

        // ─────────────────────────────────────────────
        // [2] 장소 조회 관련
        // ─────────────────────────────────────────────

        /**
         * [2-1] 현재 위치 기준 반경 내 장소 조회
         * PlaceController: getNearbyPlaces
         * - 사용자의 위도(lat), 경도(lng)를 기반으로 주변 장소 조회
         */
        public List<PlaceDto> findNearbyPlaces(double lat, double lng, double radiusMeters) {
                return placeRepository.findNearbyPlaces(lat, lng, radiusMeters).stream()
                                .map(PlaceDto::fromEntity)
                                .toList();
        }

        /**
         * [2-2] 검색어 기반 장소 조회 - 승인된 장소만
         * PlaceController: searchApprovedPlaces
         * - 승인된 장소 & 검색어 기반 조회
         */
        public List<PlaceDto> searchApprovedPlaces(String query) {
                return placeRepository.findApprovedByNameContaining(query).stream()
                                .map(PlaceDto::fromEntity)
                                .toList();
        }

        /**
         * [2-3] 장소 상세 정보 조회 (공식 장소)
         * PageController: getPlaceForRegister
         * PageController: showCommunityPage
         * PageController: showRegisterNoticePage
         * PageController: editPlacePage
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
                                place.isRejected(),
                                ownerId,
                                recentInfo,
                                communityLink,
                                allowedRequestTypes);
        }

        /**
         * [2-4] 특정 사용자가 등록한 장소 조회
         * PageController: myPlacePage
         * - 로그인 사용자 본인이 등록한 장소 목록 반환
         * - 추후에 장소 등록페이지구현때 적용
         */
        public List<PlaceDto> findByOwner(Long ownerId) {
                return placeRepository.findByOwnerId(ownerId).stream()
                                .map(PlaceDto::fromEntity) // 엔티티 → DTO 변환
                                .toList();
        }

        // ─────────────────────────────────────────────
        // [3] 장소 수정
        // ─────────────────────────────────────────────

        /**
         * [3-1] 장소 수정 메서드
         * PlaceController: updatePlace
         * - 장소 정보를 수정하는 메서드
         * - 장소 ID와 수정할 DTO를 받아 해당 장소를 찾아 업데이트
         * - 본인 소유 확인 후 수정
         * - 반려 상태였으면 초기화 (재등록 개념)
         * - 기존 허용 타입 삭제 후 새로 등록
         */
        @Transactional
        public void updatePlace(Long placeId, PlaceRegisterRequestDto dto, Long userId) {
                Place place = placeRepository.findById(placeId)
                                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다: " + placeId));

                // 본인 소유 확인
                if (!place.getOwner().getId().equals(userId)) {
                        throw new IllegalStateException("해당 장소를 수정할 권한이 없습니다.");
                }

                // 기존 필드 업데이트
                place.setName(dto.getName());
                place.setAddress(dto.getAddress());
                place.setLat(dto.getLat());
                place.setLng(dto.getLng());

                // 반려 상태였으면 초기화 (재등록 개념)
                if (place.isRejected()) {
                        place.setRejected(false);
                        place.setApproved(false);
                        place.setRejectReason(null);
                }

                // 기존 허용 타입 삭제 후 새로 등록
                allowedRequestTypeRepository.deleteByPlaceId(placeId);
                Set<AllowedRequestType> newAllowedTypes = dto.getCategories().stream()
                                .map(typeStr -> {
                                        RequestCategory category = RequestCategory.valueOf(typeStr);
                                        return new AllowedRequestType(place, category);
                                }).collect(Collectors.toSet());
                place.getAllowedRequestTypes().clear();
                place.getAllowedRequestTypes().addAll(newAllowedTypes);
        }

        // ─────────────────────────────────────────────
        // [4] 장소 즐겨찾기 (favoritePlaceRepository)
        // ─────────────────────────────────────────────

        /**
         * [4-1] 즐겨찾기 등록/해제
         * PlaceController: toggleFavoritePlace
         * - 사용자가 특정 장소를 즐겨찾기 등록 또는 해제
         */
        @Transactional
        public boolean toggleFavoritePlace(Long placeId, Long userId) {
                boolean isFavorite = favoritePlaceRepository.existsByUserIdAndPlaceId(userId, placeId);

                if (isFavorite) {
                        favoritePlaceRepository.deleteByUserIdAndPlaceId(userId, placeId);
                        return false; // 해제됨
                } else {
                        FavoritePlace favorite = new FavoritePlace();
                        favorite.setUser(userRepository.getReferenceById(userId));
                        favorite.setPlace(placeRepository.getReferenceById(placeId));
                        favoritePlaceRepository.save(favorite);
                        return true; // 등록됨
                }
        }

        /**
         * [4-2] 사용자가 해당 장소를 즐겨찾기했는지 여부 조회
         * PlaceController: isFavoritePlace
         * - 특정 사용자가 특정 장소를 즐겨찾기했는지 확인
         */
        public boolean isFavorite(Long placeId, Long userId) {
                return favoritePlaceRepository.existsByUserIdAndPlaceId(userId, placeId);
        }

        /**
         * [4-3] 사용자 즐겨찾기 목록 조회
         * PlaceController: getMyFavorites
         * - 특정 사용자가 등록한 즐겨찾기 장소를 조회
         * - FavoritePlace → FavoritePlaceDto 로 변환
         */
        public List<FavoritePlaceDto> getFavoritesByUser(Long userId) {
                return favoritePlaceRepository.findByUserId(userId).stream()
                                .map(FavoritePlaceDto::fromEntity)
                                .toList();
        }
}