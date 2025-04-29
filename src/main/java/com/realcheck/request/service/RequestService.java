package com.realcheck.request.service;

import com.realcheck.request.entity.Request;
import com.realcheck.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    // 요청 등록
    public Request createRequest(Request request) {
        return requestRepository.save(request);
    }

    // 모든 미마감 요청 조회
    public List<Request> findOpenRequests() {
        return requestRepository.findByIsClosedFalse();
    }

    // 특정 요청 조회
    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    // 요청 마감 처리
    public void closeRequest(Request request) {
        request.setClosed(true);
        requestRepository.save(request);
    }
}
