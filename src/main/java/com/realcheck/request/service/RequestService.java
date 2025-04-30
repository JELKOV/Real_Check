package com.realcheck.request.service;

import com.realcheck.request.entity.Request;
import com.realcheck.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * RequestService
 * - 요청(Request) 도메인의 비즈니스 로직을 처리하는 서비스 클래스
 * - 요청 등록, 조회, 마감 등의 기능 제공
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    /**
     * [1] 요청 등록
     * - 새 Request 엔티티를 DB에 저장
     * - 클라이언트의 요청 등록 폼을 통해 전달된 데이터를 저장할 때 사용
     *
     * @param request 저장할 요청 객체
     * @return 저장된 요청 객체 (DB에서 생성된 ID 포함)
     */
    public Request createRequest(Request request) {
        return requestRepository.save(request);
    }

    /**
     * [2] 미마감 요청 중 답변이 3개 미만인 요청 조회
     * - isClosed = false AND statusLogs.size < 3
     * - 홈 화면, 답변 대기 요청 리스트 등에서 사용
     *
     * @return 답변 부족 상태의 요청 리스트
     */
    public List<Request> findOpenRequests() {
        return requestRepository.findOpenRequestsWithoutAnswer();
    }

    /**
     * [3] 반경 내 답변 부족 요청 조회
     * - 사용자의 현재 위치(lat, lng)를 기준으로 반경(radiusMeters) 이내
     * - 마감되지 않았고 답변 수가 3개 미만인 요청만 필터링
     * - 지도 기반 요청 탐색 기능에 사용
     *
     * @param lat 위도
     * @param lng 경도
     * @param radiusMeters 반경 (미터 단위)
     * @return 반경 내 조건을 만족하는 요청 리스트
     */
    public List<Request> findNearbyWithFewAnswers(double lat, double lng, double radiusMeters) {
        return requestRepository.findNearbyFewAnswers(lat, lng, radiusMeters);
    }

    /**
     * [4] 특정 요청 ID로 요청 조회
     * - 요청 상세 페이지 등에서 사용
     *
     * @param id 요청 ID
     * @return Optional<Request> (존재하지 않을 경우 대비)
     */
    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    /**
     * [5] 요청 마감 처리
     * - 해당 요청의 isClosed 플래그를 true로 설정하고 저장
     * - 관리자가 요청을 수동 마감할 때 사용 가능
     *
     * @param request 마감할 요청 객체
     */
    public void closeRequest(Request request) {
        request.setClosed(true);
        requestRepository.save(request);
    }
}
