package com.realcheck.user.repository;

import com.realcheck.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository 인터페이스
 * - JPA를 통해 User 엔티티에 대한 DB 접근을 수행
 * - 기본 CRUD 메서드는 JpaRepository로부터 상속받아 사용 가능
 */

public interface UserRepository extends JpaRepository<User, Long> {

    // ────────────────────────────────────────
    // [1] 사용자 조회 기능 (단건 조회)
    // ────────────────────────────────────────

    /**
     * [1-1] 이메일로 사용자 정보를 조회하는 메서드
     * UserSerivce: validateUserByEmail
     * UserService: validateUniqueEmail
     * UserService: isEmailExists
     * - 회원가입 중복 검사, 로그인 시 사용자 확인에 사용
     */
    Optional<User> findByEmail(String email);

    /**
     * [1-2] 닉네임으로 사용자 정보를 조회하는 메서드
     * UserService: validateUniqueNickname
     * UserService: isNicknameExists
     * - 회원가입 중복 검사, 프로필 수정 중복 확인에 사용
     */
    Optional<User> findByNickname(String nickname);

    /**
     * [1-3] 닉네임 중복 여부를 판단하는 메서드
     * UserService: updateProfile
     */
    boolean existsByNickname(String nickname);

    // ────────────────────────────────────────
    // [2] 사용자 상태 조회 (활성/비활성)
    // ────────────────────────────────────────

    /**
     * [2-1] isActive가 false인 사용자 목록 조회하는 메서드
     * UserAdminService : getBlockedUsers
     * - 비활성화된(차단된) 사용자 조회용
     */
    List<User> findByIsActiveFalse();

    // ────────────────────────────────────────
    // [3] 사용자 검색 기능 (이메일/닉네임)
    // ────────────────────────────────────────

    /**
     * [3-1] 이메일 또는 닉네임에 검색어(keyword)가 포함된 사용자 목록 조회
     * - UserAdminService : searchUsers
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    List<User> searchByEmailOrNickname(@Param("keyword") String keyword);

    /**
     * [3-2] 삭제(회원탈퇴) 예정 상태인 사용자 조회 메서드
     * - UserDeletionScheduler: autoDeleteExpiredAccounts
     */
    List<User> findByIsPendingDeletionTrueAndDeletionScheduledAtBefore(LocalDateTime now);
}
