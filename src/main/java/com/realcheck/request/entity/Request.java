package com.realcheck.request.entity;

import com.realcheck.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 요청자

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer point; // 걸린 포인트

    private String placeId;
    private Double lat;
    private Double lng;

    private boolean isClosed;

    private LocalDateTime createdAt;
}