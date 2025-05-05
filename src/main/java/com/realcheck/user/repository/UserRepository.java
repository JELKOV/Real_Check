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
     * UserSerivce: login
     * UserService: register
     * [1] 이메일로 사용자 정보를 조회하는 메서드
     * - SQL: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * UserService: register
     * [2] 닉네임으로 사용자 정보를 조회하는 메서드
     * - SQL: SELECT * FROM users WHERE nickname = ?
     */
    Optional<User> findByNickname(String nickname);

    // ────────────────────────────────────────
    // [2] 차단 사용자 관련
    // ────────────────────────────────────────

    /**
     * UserAdminService : getBlockedUsers
     * [1] isActive가 false인 사용자 목록 조회하는 메서드
     * - 비활성화된(차단된) 사용자 조회용
     * - SQL: SELECT * FROM users WHERE is_active = false
     */
    List<User> findByIsActiveFalse();

    // ────────────────────────────────────────
    // [3] 검색 기능
    // ────────────────────────────────────────

    /**
     * UserAdminService : searchUsers
     * [1] 이메일 또는 닉네임에 검색어(keyword)가 포함된 사용자 목록 조회
     * - SQL:
     * SELECT * FROM users
     * WHERE email LIKE '%keyword%'
     * OR nickname LIKE '%keyword%'
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    List<User> searchByEmailOrNickname(@Param("keyword") String keyword);
}
