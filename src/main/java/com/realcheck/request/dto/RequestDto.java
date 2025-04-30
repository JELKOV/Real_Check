package com.realcheck.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.realcheck.request.entity.Request;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private String title;
    private String content;
    private Integer point;
    private String placeId;
    private Double lat;
    private Double lng;
    private boolean isClosed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String requesterEmail;
    private String requesterNickname;

    private int answerCount;

    /**
     * 엔티티를 DTO로 변환하는 정적 메서드
     * - 요청자 정보 일부 (이메일/닉네임) 포함
     * - 연결된 답변(StatusLog) 개수 포함
     */
    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .content(r.getContent())
                .point(r.getPoint())
                .placeId(r.getPlaceId())
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
