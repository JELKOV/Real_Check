package com.realcheck.request.dto;

import lombok.*;

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
    private String createdAt;
    private String requesterEmail;
}