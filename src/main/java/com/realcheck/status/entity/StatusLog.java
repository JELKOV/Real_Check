package com.realcheck.status.entity;

import java.time.LocalDateTime;

import com.realcheck.place.entity.Place;
import com.realcheck.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * 현장(예: 미용실 등)의 실시간 상태 정보를 저장하는 엔티티
 * - 예: "현재 대기 3명", "잠시 휴식 중" 등의 정보
 */
@Entity
@Table(name = "status_logs") // 테이블명: status_logs
@Getter @Setter
@NoArgsConstructor
public class StatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    // 상태 설명 (ex: "현재 대기 3명", "예약만 가능")
    private String content;

    // 대기 인원 수 (숫자로 따로 저장)
    private int waitCount;

    // 업로드된 사진이 있을 경우의 경로 (선택적)
    private String imageUrl;

    // 등록 시각 – 기본값은 현재 시간
    private LocalDateTime createdAt = LocalDateTime.now();

    // 이 정보를 등록한 사용자 정보 (Many-to-One 관계)
    @ManyToOne
    @JoinColumn(name = "user_id") // FK 이름은 user_id
    private User reporter;

    // 해당 정보가 속한 장소
    @ManyToOne
    @JoinColumn(name = "place_id") // FK 이름은 place_id
    private Place place;
}
