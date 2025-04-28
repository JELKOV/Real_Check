package com.realcheck.status.dto;

import java.time.LocalDateTime;

import com.realcheck.place.entity.Place;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.user.entity.User;
import lombok.*;

/**
 * StatusLogDto 클래스
 * - 클라이언트 ↔ 서버 간 대기 정보(StatusLog)를 전달할 때 사용하는 데이터 전송 객체 (DTO)
 * - Entity와는 다르게 DB와 직접 연결되지 않으며, 필요한 데이터만 추려서 담는다
 * - 주로 컨트롤러와 서비스 간 전달, 응답 포맷 구성에 사용
 */

@Data // 모든 필드에 대해 Getter, Setter, equals, hashCode, toString 메서드를 자동 생성
@NoArgsConstructor // 기본 생성자 (파라미터 없는 생성자) 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class StatusLogDto {

    private Long id;
    private String content; // 대기 상황 설명 (예: "현재 3명 대기 중")
    private int waitCount; // 대기 인원 수
    private String imageUrl; // 이미지 URL (선택 사항)
    private Long placeId; // 장소 ID (어떤 장소의 대기 정보인지)
    private LocalDateTime createdAt; // 등록 일시

    /**
     * DTO → Entity 변환 메서드
     * - 컨트롤러/서비스에서 DTO를 받아 실제 DB에 저장할 수 있도록 Entity로 변환
     *
     * @param user  등록자 (세션에서 가져온 로그인 유저)
     * @param place 해당 대기 정보가 속한 장소 객체
     * @return StatusLog 엔티티 객체
     */
    public StatusLog toEntity(User user, Place place) {
        StatusLog log = new StatusLog();
        log.setContent(this.content);
        log.setWaitCount(this.waitCount);
        log.setImageUrl(this.imageUrl);
        log.setReporter(user); // 해당 StatusLog를 등록한 사용자
        log.setPlace(place); // 관련 장소 정보
        return log;
    }

    /**
     * Entity → DTO 변환 메서드
     * - DB에 저장된 StatusLog 엔티티를 클라이언트에 응답할 DTO 형태로 변환
     *
     * @param log StatusLog 엔티티
     * @return StatusLogDto
     */
    public static StatusLogDto fromEntity(StatusLog log) {
        return new StatusLogDto(
                log.getId(),
                log.getContent(),
                log.getWaitCount(),
                log.getImageUrl(),
                log.getPlace().getId(),
                log.getCreatedAt());
    }
}
