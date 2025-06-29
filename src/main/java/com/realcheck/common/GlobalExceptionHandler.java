package com.realcheck.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 전역 예외 처리 클래스
 * - @RestControllerAdvice를 사용해 모든 컨트롤러에서 발생하는 예외를 중앙에서 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException 처리
     * - 유효하지 않은 입력 값, 포인트 부족 등 사용자 잘못에 의한 예외
     * - 클라이언트가 잘못된 요청을 보낸 것이므로 HTTP 400 (Bad Request) 반환
     *
     * @param ex 발생한 예외
     * @return ResponseEntity<String> 예외 메시지와 함께 400 상태 코드 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /**
     * 모든 예외에 대한 기본 처리
     * - 위에서 명시적으로 처리되지 않은 모든 예외를 처리
     * - 서버 측 문제일 가능성이 높으므로 HTTP 500 (Internal Server Error) 반환
     *
     * @param ex 발생한 예외
     * @return ResponseEntity<String> 고정된 메시지와 함께 500 상태 코드 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        ex.printStackTrace(); // 서버 콘솔에 스택트레이스를 출력해 디버깅에 도움
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다.");
    }
}
