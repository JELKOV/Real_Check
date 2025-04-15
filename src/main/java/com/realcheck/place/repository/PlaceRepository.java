package com.realcheck.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.realcheck.place.entity.Place;

/**
 * PlaceRepository 인터페이스
 * - 장소(Place) 엔티티와 관련된 DB 접근 로직을 처리
 * - JpaRepository<Place, Long>을 상속하면 기본적인 CRUD 기능이 자동으로 제공됨
 * - 필요에 따라 사용자 정의 쿼리 메서드를 추가할 수 있음
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {
    /**
     * 특정 사용자가 등록한 장소들을 조회하는 메서드
     * - 메서드 이름 기반으로 Spring Data JPA가 자동으로 쿼리를 생성함
     * - SELECT * FROM places WHERE owner_id = ? 와 유사한 쿼리 실행
     *
     * @param ownerId 사용자(등록자)의 고유 ID
     * @return 해당 사용자가 등록한 모든 장소 리스트
     */
    List<Place> findByOwnerId(Long ownerId);
}
