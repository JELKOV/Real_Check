package com.realcheck.user.repository;

import com.realcheck.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository 인터페이스
 * - JPA를 통해 User 엔티티에 대한 DB 접근을 수행
 * - 기본 CRUD 메서드는 JpaRepository로부터 상속받아 사용 가능
 */

public interface UserRepository extends JpaRepository<User, Long> {

    // ────────────────────────────────────────
    // [1] 사용자 단건 조회
    // ────────────────────────────────────────

    /**
     * [1-1] 이메일로 사용자 정보를 조회하는 메서드
     *
     * - 메서드 네이밍 규칙 기반 자동 쿼리 생성
     * - SQL: SELECT * FROM users WHERE email = ?
     * 
     * @param email 조회할 사용자 이메일
     * @return Optional<User> - 존재하면 User 객체, 없으면 빈 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * [1-2] 닉네임으로 사용자 정보를 조회하는 메서드
     *
     * - SQL: SELECT * FROM users WHERE nickname = ?
     * 
     * @param nickname 조회할 사용자 닉네임
     * @return Optional<User> - 존재하면 User 객체, 없으면 빈 Optional
     */
    Optional<User> findByNickname(String nickname);

    // ────────────────────────────────────────
    // [2] 차단 사용자 관련
    // ────────────────────────────────────────

    /**
     * [2-1] isActive가 false인 사용자 목록 조회하는 메서드
     *
     * - 비활성화된(차단된) 사용자 조회용
     * - SQL: SELECT * FROM users WHERE is_active = false
     * 
     * @return isActive가 false인 User 객체 리스트
     */
    List<User> findByIsActiveFalse();

    // ────────────────────────────────────────
    // [3] 검색 기능
    // ────────────────────────────────────────

    /**
     * [3-1] 이메일 또는 닉네임에 검색어(keyword)가 포함된 사용자 목록 조회
     *
     * - @Query를 활용한 JPQL 쿼리 정의
     * - SQL:
     * SELECT * FROM users
     * WHERE email LIKE '%keyword%'
     * OR nickname LIKE '%keyword%'
     *
     * @param keyword 검색어 (부분 일치)
     * @return 일치하는 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    List<User> searchByEmailOrNickname(@Param("keyword") String keyword);
}
