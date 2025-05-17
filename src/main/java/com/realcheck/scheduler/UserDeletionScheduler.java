package com.realcheck.scheduler;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realcheck.user.entity.User;
import com.realcheck.user.repository.UserRepository;
import com.realcheck.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionScheduler {
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 매일 자정에 탈퇴 예정 사용자 삭제
     */
    @Scheduled(cron = "0 0 0 * * *")// 매일 자정
    @Transactional
    public void autoDeleteExpiredAccounts() {
        List<User> usersToDelete = userRepository
                .findByIsPendingDeletionTrueAndDeletionScheduledAtBefore(LocalDateTime.now());

        for (User user : usersToDelete) {
            try {
                userService.deleteUserAndRelatedData(user.getId());
                log.info("회원 탈퇴 처리 완료: {}", user.getEmail());
            } catch (Exception e) {
                log.error("회원 탈퇴 처리 중 오류 발생 (ID: {}): {}", user.getId(), e.getMessage());
            }
        }
    }
}
