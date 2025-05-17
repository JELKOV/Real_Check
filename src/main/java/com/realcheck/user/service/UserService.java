package com.realcheck.user.service;

import com.realcheck.request.repository.RequestRepository;
import com.realcheck.status.repository.StatusLogRepository;
import com.realcheck.user.dto.PasswordUpdateRequestDto;
import com.realcheck.user.dto.UserDto;
import com.realcheck.user.entity.User;
import com.realcheck.user.entity.UserRole;
import com.realcheck.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService 클래스
 * - 사용자 관련 비즈니스 로직 (회원가입, 로그인, 비밀번호 변경 등)을 처리
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestRepository requestRepository;
    private final StatusLogRepository statusLogRepository;

    // ─────────────────────────────────────────────
    // [1] 사용자 생성 및 인증 관련 기능
    // ─────────────────────────────────────────────

    /**
     * RegisterController: register
     * [1-1] 회원가입 처리
     * 이메일, 닉네임 중복 확인
     * 비밀번호 암호화 후 저장
     */
    public void register(UserDto dto, String rawPassword) {
        // (1) 이메일 중복 검사
        validateUniqueEmail(dto.getEmail());

        // (2) 닉네임 중복 검사
        validateUniqueNickname(dto.getNickname());

        // (3) DTO → Entity 변환
        User user = dto.toEntity();

        // (4) 비밀번호 암호화 적용
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // (5) 기본 정보 세팅 (역할, 포인트, 활성 상태)
        // 회원가입은 기본적으로 일반 사용자로 설정
        user.setRole(UserRole.USER);
        user.setPoints(0); // 기본 포인트는 0
        user.setActive(true); // 계정은 기본적으로 활성화 상태

        // (6) DB에 저장
        userRepository.save(user);
    }

    /**
     * [1-1-A] 이메일 중복 확인
     */
    private void validateUniqueEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
    }

    /**
     * [1-1-B] 닉네임 중복 확인
     */
    private void validateUniqueNickname(String nickname) {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
    }

    /**
     * LoginController: login
     * [1-2] 로그인 처리
     * 이메일로 사용자 조회
     * 비밀번호 비교 (암호화된 비밀번호와 비교)
     * 성공 시 DTO 반환
     */
    public UserDto login(String email, String rawPassword) {
        // (1) 이메일로 사용자 조회 (존재하지 않으면 예외 발생)
        User user = validateUserByEmail(email);

        // (2) 탈퇴 예약 상태 확인 (로그인 차단)
        if (user.isPendingDeletion()) {
            throw new RuntimeException("현재 계정은 탈퇴 예약 상태입니다. 서비스 이용이 제한됩니다.");
        }

        // (3) 입력한 비밀번호(rawPassword)와 저장된 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // (4) 마지막 로그인 시간 업데이트
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // (5) 로그인 성공 → User 엔티티를 DTO로 변환하여 반환 (비밀번호 제외)
        return UserDto.fromEntity(user);
    }

    /**
     * [1-2-A]사용자 존재 여부 확인 (Email)
     */
    private User validateUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));
    }

    /**
     * RequestController: createRequest
     * AnswerController: createAnswer
     * StatusLogController: register / registerFreeShare / updateStatusLog /
     * selectAnswer
     * [1-3] UserDto → User 변환 (DB에서 조회하여 변환)
     */
    public User convertToUser(UserDto dto) {
        return userRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // ─────────────────────────────────────────────
    // [2] 사용자 정보 관리 기능 (마이페이지)
    // ─────────────────────────────────────────────

    /**
     * UserController: updateProfile
     * [2-1] 사용자 프로필 수정
     * 닉네임 또는 비밀번호 변경
     * 닉네임은 단순 대체, 비밀번호는 암호화 후 저장
     */
    public void updateProfile(Long id, UserDto dto) {
        // (1) 해당 ID의 사용자 정보 조회 (없으면 예외 발생)
        User user = validateUserById(id);

        // (2) 닉네임이 전달된 경우 → 기존 닉네임을 새 닉네임으로 변경
        if (dto.getNickname() != null)
            user.setNickname(dto.getNickname());

        // (3) 비밀번호가 전달된 경우 → 암호화 후 저장
        if (dto.getPassword() != null) {
            String encrypted = passwordEncoder.encode(dto.getPassword());
            user.setPassword(encrypted);
        }

        // (4) 변경된 사용자 정보를 DB에 저장
        userRepository.save(user);
    }

    /**
     * UserController: changePassword
     * [2-2] 비밀번호 변경
     * 현재 비밀번호 확인 후 새 비밀번호로 변경
     */
    public void changePassword(Long userId, PasswordUpdateRequestDto dto) {
        User user = validateUserById(userId);

        // 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        // 새 비밀번호로 변경 (암호화)
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * [2-1/2-A] 사용자 존재 여부 확인 (ID)
     */
    private User validateUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
    }

    /**
     * UserController: checkEmail
     * [2-3] 이메일 중복 여부 확인 (AJAX)
     * 회원가입/프로필 수정 시 사용
     */
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * UserController: checkNickname
     * [2-4] 닉네임 중복 여부 확인 (AJAX)
     * 회원가입/프로필 수정 시 사용
     */
    public boolean isNicknameExists(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    /**
     * PageController: myPage
     * [2-5] 최근 활동 조회 (요청, 답변)
     */
    public List<Map<String, Object>> getRecentActivities(Long userId) {
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 최근 요청 5개
        requestRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).forEach(req -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "요청");
            activity.put("title", req.getTitle());
            activity.put("category", req.getCategory().name());

            // 장소 정보 (공식 장소 or 사용자 지정)
            if (req.getPlace() != null) {
                activity.put("placeName", req.getPlace().getName());
            } else if (req.getCustomPlaceName() != null) {
                activity.put("placeName", "사용자 지정 장소 - " + req.getCustomPlaceName());
            } else {
                activity.put("placeName", "좌표: (" + req.getLat() + ", " + req.getLng() + ")");
            }

            activity.put("createdAt", req.getCreatedAt().format(formatter));
            recentActivities.add(activity);
        });
        // 최근 답변 5개
        statusLogRepository.findTop5ByReporterIdOrderByCreatedAtDesc(userId).forEach(log -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "답변");
            activity.put("content", log.getContent());

            // 요청 정보 (답변일 경우)
            if (log.getRequest() != null) {
                activity.put("category", log.getRequest().getCategory().name());
                activity.put("requestTitle", log.getRequest().getTitle());

                // 장소 정보 (공식 장소 or 사용자 지정)
                if (log.getRequest().getPlace() != null) {
                    activity.put("placeName", log.getRequest().getPlace().getName());
                } else if (log.getRequest().getCustomPlaceName() != null) {
                    activity.put("placeName", "사용자 지정 장소 - " + log.getRequest().getCustomPlaceName());
                } else {
                    activity.put("placeName",
                            "좌표: (" + log.getRequest().getLat() + ", " + log.getRequest().getLng() + ")");
                }
            } else {
                // 답변이 자유 공유일 경우
                activity.put("category", log.getStatusType().name());
                activity.put("requestTitle", "자유 공유");
                activity.put("placeName", log.getPlace() != null ? log.getPlace().getName() : "장소 정보 없음");
            }

            activity.put("createdAt", log.getCreatedAt().format(formatter));
            recentActivities.add(activity);
        });

        // 최신순 정렬
        recentActivities.sort(
                Comparator.comparing(activity -> (String) activity.get("createdAt"), Comparator.reverseOrder()));

        // 최대 5개만 반환
        return recentActivities.stream().limit(5).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // [3] 사용자 탈퇴 관련 기능 (마이페이지)
    // ─────────────────────────────────────────────

    /**
     * UserController: requestAccountDeletion
     * [3-1] 회원 탈퇴 요청 처리 (7 일간 유예)
     */
    public void requestAccountDeletion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setPendingDeletion(true);
        user.setDeletionScheduledAt(LocalDateTime.now().plusDays(7));
        userRepository.save(user);
    }

    /**
     * LoginController: login (Post)
     * [3-2] 회원 탈퇴 예약 취소 (7 일간 유예)
     */
    public void cancelAccountDeletion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setPendingDeletion(false);
        user.setDeletionScheduledAt(null);
        userRepository.save(user);
    }

    /**
     * [3-2-A] 사용자 정보 조회
     * 로그인 유저 정보 갱신용
     */
    public UserDto getUserDtoById(Long userId) {
        return userRepository.findById(userId)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * UserDeletionScheduler:autoDeleteExpiredAccounts
     * [3-3] 회원 및 관련 데이터 삭제 (요청, 답변) - 트랜잭션 적용
     */
    @Transactional
    public void deleteUserAndRelatedData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // (1) 사용자 관련 데이터 삭제 (요청, 답변)
        requestRepository.deleteByUserId(userId);
        statusLogRepository.deleteByReporterId(userId);

        // (2) 사용자 삭제
        userRepository.delete(user);
    }

    // ─────────────────────────────────────────────
    // [4] 관리자 계정 자동 생성 (초기화)
    // ─────────────────────────────────────────────

    /**
     * [4-1] 관리자 계정 자동 생성 (초기화)
     * 서버 시작 시 admin@example.com 계정이 없으면 자동 생성
     */
    @PostConstruct
    public void insertAdminAccount() {
        System.out.println("insertAdminAccount 실행됨");

        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setNickname("관리자");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            admin.setPoints(0);
            admin.setPassword(passwordEncoder.encode("admin1234"));

            userRepository.save(admin);
            System.out.println("관리자 계정 생성 완료");
        } else {
            System.out.println("이미 admin@example.com 계정이 존재합니다.");
        }
    }
}
