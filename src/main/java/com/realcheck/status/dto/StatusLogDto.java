package com.realcheck.status.dto;

import java.time.LocalDateTime;

import com.realcheck.place.entity.Place;
import com.realcheck.status.entity.StatusLog;
import com.realcheck.status.entity.StatusType;
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

    private Long placeId; // 공식 장소 ID (null 가능)
    private Double lat; // 위치 위도 (커스텀 장소 대응)
    private Double lng; // 위치 경도

    private LocalDateTime createdAt; // 등록 일시
    private StatusType type;
    private Long requestId;

    /**
     * DTO → Entity 변환 메서드
     * - 컨트롤러/서비스에서 DTO를 받아 실제 DB에 저장할 수 있도록 Entity로 변환
     */
    public StatusLog toEntity(User user, Place place) {
        StatusLog log = new StatusLog();
        log.setContent(this.content);
        log.setWaitCount(this.waitCount);
        log.setImageUrl(this.imageUrl);
        log.setReporter(user);
        log.setPlace(place);
        log.setStatusType(this.type != null ? this.type : StatusType.ANSWER);

        if (place != null) {
            log.setLat(place.getLat());
            log.setLng(place.getLng());
        } else {
            log.setLat(this.lat);
            log.setLng(this.lng);
        }

        return log;
    }

    /**
     * Entity → DTO 변환 메서드
     * - DB에 저장된 StatusLog 엔티티를 클라이언트에 응답할 DTO 형태로 변환
     */
    public static StatusLogDto fromEntity(StatusLog log) {
        return new StatusLogDto(
                log.getId(),
                log.getContent(),
                log.getWaitCount(),
                log.getImageUrl(),
                log.getPlace() != null ? log.getPlace().getId() : null,
                log.getLat(),
                log.getLng(),
                log.getCreatedAt(),
                log.getStatusType(),
                log.getRequest() != null ? log.getRequest().getId() : null);
    }
}
