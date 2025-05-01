package com.realcheck.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.realcheck.request.entity.Request;
import com.realcheck.user.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RequestDto
 * - 클라이언트 ↔ 서버 간 요청 정보를 주고받기 위한 DTO 클래스
 * - Request Entity와 분리된 구조로, 필요한 필드만 직렬화/역직렬화
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    // 요청 ID (PK)
    private Long id;
    // 요청 제목
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    // 요청 상세 설명
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    // 요청에 걸린 포인트
    @NotNull(message = "포인트는 필수입니다.")
    private Integer point;

    // 유저가 직접 입력한 장소 이름 (ex. "문방구 앞 붕어빵")
    private String customPlaceName;
    // 등록된 Place 객체의 이름 (공식 장소 이름)
    private String placeName;
    // 위도 / 경도 정보 (지도에 표시하기 위함)
    private Double lat;
    private Double lng;

    // 요청 마감 여부 (답변 채택 시 true)
    private boolean isClosed;

    // 요청 등록 시각
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 요청자 이메일 / 닉네임
    private String requesterEmail;
    private String requesterNickname;

    // 해당 요청에 달린 답변 수
    private int answerCount;

    /**
     * DTO → Entity 변환 메서드
     * - 클라이언트가 보낸 데이터를 실제 DB에 저장 가능한 형태로 변환
     *
     * @param user 현재 로그인한 사용자 (요청자)
     * @return Request 엔티티 객체
     */
    public Request toEntity(User user) {
        if (placeName == null && (customPlaceName == null || lat == null || lng == null)) {
            throw new IllegalArgumentException("장소 정보가 누락되었습니다.");
        }
        
        return Request.builder()
                .user(user)
                .title(title)
                .content(content)
                .point(point)
                .customPlaceName(customPlaceName)
                .lat(lat)
                .lng(lng)
                .createdAt(LocalDateTime.now())
                .isClosed(false)
                .build();
    }

    /**
     * Entity → DTO 변환 메서드
     * - DB에서 꺼낸 Request 객체를 클라이언트에게 전달 가능한 형태로 변환
     *
     * @param r Request 엔티티
     * @return 변환된 DTO 객체
     */
    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .content(r.getContent())
                .point(r.getPoint())
                .customPlaceName(r.getCustomPlaceName())
                .placeName(r.getPlace() != null ? r.getPlace().getName() : null)
                .lat(r.getLat())
                .lng(r.getLng())
                .isClosed(r.isClosed())
                .createdAt(r.getCreatedAt())
                .requesterEmail(r.getUser() != null ? r.getUser().getEmail() : null)
                .requesterNickname(r.getUser() != null ? r.getUser().getNickname() : null)
                .answerCount(r.getStatusLogs() != null ? r.getStatusLogs().size() : 0)
                .build();
    }
}
