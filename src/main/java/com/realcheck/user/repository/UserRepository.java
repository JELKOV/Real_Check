package com.realcheck.user.repository;

import com.realcheck.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository 인터페이스
 * - JPA를 활용해 User 엔티티에 대한 데이터베이스 접근을 처리
 * - JpaRepository<User, Long>을 상속하여 기본 CRUD 메서드 자동 제공
 * - 사용자 정의 쿼리 메서드도 추가 가능
 */

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일로 사용자 정보를 조회하는 메서드
     * - JPA가 메서드 이름을 기반으로 쿼리를 자동 생성
     * - SELECT * FROM users WHERE email = ? 와 유사한 동작
     *
     * @param email 조회할 사용자 이메일
     * @return Optional<User> - 존재하면 User 객체, 없으면 빈 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 정보를 조회하는 메서드
     * - SELECT * FROM users WHERE nickname = ? 와 유사하게 동작
     *
     * @param nickname 조회할 사용자 닉네임
     * @return Optional<User> - 존재하면 User 객체, 없으면 빈 Optional
     */
    Optional<User> findByNickname(String nickname);
}
