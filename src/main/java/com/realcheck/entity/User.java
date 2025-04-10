package com.realcheck.entity;

// JPA에서 DB 매핑을 위한 어노테이션 제공
import jakarta.persistence.*;
// 코드를 짧게 줄여주는 자동 생성 도구 (생성자, getter/setter)
import lombok.*;

//JPA ENTITY DB 테이블로 매핑 됨
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    //PK 필드
    @Id 
    //ID를 자동 증가하도록 설정
    //IDENTITY 전략은 MySQL의 auto_increment 방식과 동일
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;   
}
