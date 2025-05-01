package com.realcheck.place.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realcheck.place.dto.PlaceDto;
import com.realcheck.place.entity.Place;
import com.realcheck.place.repository.PlaceRepository;
import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * PlaceService 클래스
 * - 장소(Place) 관련 비즈니스 로직을 담당하는 서비스 계층
 * - 컨트롤러로부터 요청을 받아 실제 동작을 처리하고, DB 작업은 Repository에 위임함
 */
@Service // 이 클래스가 스프링의 서비스 컴포넌트임을 명시 → 자동으로 Bean 등록됨
@RequiredArgsConstructor // final로 선언된 필드를 생성자로 자동 주입 (DI)
public class PlaceService {
    private final PlaceRepository placeRepository; // 장소 데이터를 저장/조회하는 JPA Repository
    private final UserRepository userRepository; // 장소를 등록한 사용자 정보를 조회하기 위한 Repository

    // ─────────────────────────────────────────────
    // [1] 장소 등록 관련
    // ─────────────────────────────────────────────

    /**
     * [1-1] 장소 등록 메서드
     * - 등록자의 ID(ownerId)를 기반으로 User 객체를 조회하고,
     * - PlaceDto를 Place Entity로 변환하여 DB에 저장함
     *
     * @param dto 장소 등록 요청 정보 (장소 이름, 주소, 위도/경도 등 포함)
     */
    public void registerPlace(PlaceDto dto) {
        // 1. 등록자(ownerId)를 통해 User 객체 조회
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("등록자(User ID: " + dto.getOwnerId() + ")를 찾을 수 없습니다."));
        // 2. DTO → Entity 변환
        Place place = dto.toEntity(owner);
        // 3. DB에 장소 저장
        placeRepository.save(place);
    }

    // ─────────────────────────────────────────────
    // [2] 장소 조회 관련
    // ─────────────────────────────────────────────

    /**
     * [2-1] 전체 장소 목록 조회
     * - DB에서 모든 Place 엔티티를 조회하고, PlaceDto로 변환하여 반환
     *
     * @return 전체 장소 리스트
     */
    public List<PlaceDto> findAll() {
        return placeRepository.findAll().stream()
                .map(PlaceDto::fromEntity) // 엔티티 → DTO 변환
                .toList();
    }

    /**
     * [2-2] 특정 사용자가 등록한 장소 조회
     *
     * @param ownerId 사용자(등록자)의 ID
     * @return 해당 사용자가 등록한 장소 리스트
     */
    public List<PlaceDto> findByOwner(Long ownerId) {
        return placeRepository.findByOwnerId(ownerId).stream()
                .map(PlaceDto::fromEntity) // 엔티티 → DTO 변환
                .toList();
    }

    /**
     * [2-3] 현재 위치 기준 반경 내 장소 조회
     * - 사용자의 위도(lat), 경도(lng)를 기반으로 주변 장소 조회
     *
     * @param lat          위도
     * @param lng          경도
     * @param radiusMeters 검색 반경 (미터 단위)
     * @return 반경 내 장소 리스트
     */
    public List<PlaceDto> findNearbyPlaces(double lat, double lng, double radiusMeters) {
        return placeRepository.findNearbyPlaces(lat, lng, radiusMeters).stream()
                .map(PlaceDto::fromEntity)
                .toList();
    }

    /**
     * [2-4] 승인된 장소만 필터링
     * - 사용자에게 보여줄 장소를 제한함
     * @param lat
     * @param lng
     * @param radiusMeters
     * @return
     */
    public List<PlaceDto> findApprovedNearby(double lat, double lng, double radiusMeters) {
        return placeRepository.findApprovedNearby(lat, lng, radiusMeters)
                .stream()
                .map(PlaceDto::fromEntity)
                .toList();
    }
}