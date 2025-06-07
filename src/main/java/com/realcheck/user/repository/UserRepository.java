package com.realcheck.user.repository;

import com.realcheck.admin.dto.MonthlyUserStatDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.entity.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository 인터페이스 (ALL DONE)
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

    /**
     * [3-3] 특정 역할(Role)에 해당하는 사용자 목록 조회
     * UserService: findAllAdmins
     * - 관리자 페이지에서 특정 역할을 가진 사용자 목록 조회
     */
    List<User> findByRole(UserRole role);

    /**
     * [3-4] 월별 가입 통계 조회
     * AdminStatsService: getMonthlyUserSignUpStats
     * - 사용자 가입 시점(createdAt)을 기준으로 연/월별 가입 수를 집계
     * - 프론트 대시보드에서 사용자 성장 추세를 파악하는 데 사용
     * - 결과는 연도와 월별로 그룹화되어 반환
     * - 예시: 2023년 10월에 가입한 사용자 수를 반환
     * - 반환 타입: List<MonthlyUserStatDto>
     */
    @Query("""
              SELECT new com.realcheck.admin.dto.MonthlyUserStatDto(
                YEAR(u.createdAt),
                MONTH(u.createdAt),
                COUNT(u)
              )
              FROM User u
              GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
              ORDER BY YEAR(u.createdAt) ASC, MONTH(u.createdAt) ASC
            """)
    List<MonthlyUserStatDto> countMonthlySignUps();

    /**
     * [3-5] 잔액 양수/0/ 사용자 개수를 각각 계산해서 배열로 반환
     * AdminPointService: getPointBalanceDistribution
     * 사용자 잔액(포인트) 상태를 집계하여
     * - 양수인 사용자 수
     * - 0인 사용자 수
     */
    @Query("""
                SELECT
                  SUM(CASE WHEN u.points > 0 THEN 1 ELSE 0 END),
                  SUM(CASE WHEN u.points = 0 THEN 1 ELSE 0 END)
                FROM User u
            """)
    List<Object[]> countUserByPointBalance();

    /**
     * [3-6] 잔액(=points) 높은 사용자 Top 10
     * AdminPointService: getTopUsersByPoints
     * - 사용자 잔액(포인트) 기준으로 내림차순 정렬하여 상위 10명 조회
     * - 관리자 대시보드에서 포인트가 높은 사용자 목록을 조회할 때 사용
     * - 반환 타입: List<User>
     */
    List<User> findTop10ByOrderByPointsDesc();

    /**
     * [3-7] 0원인 사용자 수
     * AdimnPointService: getZeroBalanceRatio
     * - 잔액이 0인 사용자의 비율을 계산하여 반환
     * - 전체 사용자 대비 잔액이 0인 사용자의 비율을 계산
     * - 반환 타입: double (0.0 ~ 1.0 사이의 값)
     */
    long countByPoints(int points);
}
