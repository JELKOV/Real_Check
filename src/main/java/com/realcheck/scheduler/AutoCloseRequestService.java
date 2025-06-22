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
import com.realcheck.user.entity.User;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoCloseRequestService {
    private final RequestService requestService;
    private final PointService pointService;

    /**
     * 3시간 경과 시 자동 마감 스케줄러 (5분마다 실행) TODO: 이후에 시간 체크
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    @Transactional
    public void autoCloseExpiredRequests() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(3);
        List<Request> openRequests = requestService.findOpenRequestsWithAnswers(threshold);

        // [디버깅용 로그]
        System.out.println("[AutoClose] " + LocalDateTime.now() +
                " - 자동 마감 대상 요청 수: " + openRequests.size());

        for (Request request : openRequests) {
            if (request.isClosed() || request.getStatusLogs().stream().anyMatch(StatusLog::isSelected)) {
                continue; // 이미 마감되거나 채택된 답변이 있는 경우 제외
            }

            request.setClosed(true); // 마감 처리
            try {
                // 요청 상태를 마감으로 변경
                if (!request.isPointHandled()) {
                    // 자동 마감 처리 직후, 요청자에게 포인트 차감
                    User requester = request.getUser();
                    int totalPoints = request.getPoint();

                    pointService.givePoint(requester, -totalPoints, "자동 마감으로 포인트 차감", PointType.DEDUCT);
                    // 답변자에게 포인트 분배
                    distributePointsToAnswerers(request);

                    request.setPointHandled(true); // 분배 완료 플래그
                }
                requestService.save(request);

                // [디버깅용 로그]
                System.out.println("[AutoClose] 요청 ID " + request.getId() + " 마감 성공");

            } catch (ObjectOptimisticLockingFailureException e) {
                System.out.println("[AutoClose] 요청 ID " + request.getId() +
                        " 마감 중 충돌 발생 - 다른 프로세스에서 처리했을 수 있음");
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
        List<StatusLog> visibleAnswers = request.getStatusLogs().stream()
                .filter(log -> !log.isHidden())
                .toList();

        int answerCount = visibleAnswers.size();
        if (answerCount == 0)
            return;

        int totalPoints = request.getPoint();
        int pointPerUser = totalPoints / answerCount; // 소수점 제외

        for (StatusLog answer : visibleAnswers) {
            pointService.givePoint(answer.getReporter(), pointPerUser, "자동 마감 포인트 분배", PointType.EARN);
        }
    }
}