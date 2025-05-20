package com.realcheck.scheduler;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.point.entity.PointType;
import com.realcheck.point.service.PointService;
import com.realcheck.request.entity.Request;
import com.realcheck.request.service.RequestService;
import com.realcheck.status.entity.StatusLog;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoCloseRequestService {
    private final RequestService requestService;
    private final PointService pointService;

    /**
     * 3시간 경과 시 자동 마감 스케줄러 (5분마다 실행)
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    @Transactional
    public void autoCloseExpiredRequests() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(3);
        List<Request> openRequests = requestService.findOpenRequestsWithAnswers(threshold);

        for (Request request : openRequests) {
            if (request.isClosed() || request.getStatusLogs().stream().anyMatch(StatusLog::isSelected)) {
                continue; // 이미 마감되거나 채택된 답변이 있는 경우 제외
            }

            request.setClosed(true); // 마감 처리
            try {
                requestService.save(request);
                // 답변자에게 포인트 분배
                distributePointsToAnswerers(request);
            } catch (ObjectOptimisticLockingFailureException e) {
                // 충돌 무시하고 넘어감 (다른 프로세스가 이미 처리했을 수 있음)
                System.out.println("요청 ID " + request.getId() + " 마감 중 충돌 발생. 다른 프로세스에서 이미 마감했을 수 있음.");
            }
        }
    }

    /**
     * 포인트를 답변자에게 균등 분배 (소수점 제외)
     * - 최소 포인트 1점 이상만 분배
     * - PointType.EARN 으로 기록
     */
    @Transactional
    private void distributePointsToAnswerers(Request request) {
        List<StatusLog> answers = request.getStatusLogs();
        int answerCount = answers.size();
        if (answerCount == 0)
            return;

        int totalPoints = request.getPoint();
        int pointPerUser = totalPoints / answerCount; // 소수점 제외

        // 최소 1점 이상일 때만 분배
        if (pointPerUser < 1) {
            request.setClosed(true);
            requestService.save(request);
            return;
        }

        for (StatusLog answer : answers) {
            pointService.givePoint(answer.getReporter(), pointPerUser, "자동 마감 포인트 분배", PointType.EARN);
        }
    }
}