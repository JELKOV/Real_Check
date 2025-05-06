package com.realcheck.request.service;

import com.realcheck.request.dto.RequestDto;
import com.realcheck.request.entity.Request;
import com.realcheck.request.repository.RequestRepository;
import com.realcheck.user.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RequestService
 * - 요청(Request) 관련 비즈니스 로직 처리
 * - 요청 등록, 조회, 마감 처리 담당
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    /**
     * [1] 요청 등록
     * - RequestDto를 엔티티로 변환하고 DB에 저장
     * - 변환 로직은 DTO 내부에 캡슐화되어 있음 (toEntity)
     */
    public Request createRequest(RequestDto dto, User user) {

        if (dto.getCategory() == null) {
            throw new IllegalArgumentException("질문의 카테고리를 선택해주세요.");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("질문의 제목을 입력해주세요.");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new IllegalArgumentException("질문의 내용을 입력해주세요.");
        }

        return requestRepository.save(dto.toEntity(user));
    }

    /**
     * RequestController: findOpenRequests
     * [2] 미마감 + 답변 3개 미만 요청 조회
     * - 조건: isClosed = false AND statusLogs.size < 3
     */
    public List<Request> findOpenRequests() {
        return requestRepository.findOpenRequestsWithoutAnswer();
    }

    /**
     * RequestController: findNearbyOpenRequests
     * [3] 반경 내 열린 요청 목록 조회
     * - 위도, 경도 기준으로 radius(m) 이내
     * - 장소 좌표가 존재하며 답변 수가 3개 미만인 요청 필터
     * - [FIX] 3시간 이내 수정 (테스트라 48시간으로 바꿈)
     */
    public List<Request> findNearbyValidRequests(double lat, double lng, double radiusMeters) {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(48);
        return requestRepository.findNearbyValidRequests(lat, lng, radiusMeters, timeLimit);
    }

    /**
     * RequestController: findRequestById
     * [4] ID로 요청 단건 조회
     * - 상세 보기 등에서 사용
     */
    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    /**
     * (미사용)
     * [5] 요청 마감 처리
     * - isClosed = true 설정 후 저장
     * - 답변 채택 후 호출되며, 요청을 닫기 위한 메서드
     */
    public void closeRequest(Request request) {
        request.setClosed(true);
        requestRepository.save(request);
    }

    /**
     * RequestController: findMyRequests
     * [6] 특정 사용자(userId)의 요청 목록 조회
     */
    public List<Request> findByUserId(Long userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
