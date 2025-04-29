package com.realcheck.util;

import java.util.UUID;

/**
 * FileUtil
 * - 파일 관련 공통 유틸리티 기능 제공
 * - 주로 파일명 생성, 확장자 체크 기능 담당
 */
public class FileUtil {

    /**
     * 원본 파일명으로부터 고유한 파일명을 생성
     * - UUID를 사용하여 파일명을 랜덤하게 생성
     * - 원본 확장자는 그대로 유지 (.jpg, .png 등)
     *
     * @param originalFilename 업로드된 파일의 원본 이름
     * @return 고유한 새 파일명 (예: UUID값.jpg)
     */
    public static String generateUniqueFileName(String originalFilename) {
        // 마지막 점(.) 이후를 확장자로 간주
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + ext;
    }

    /**
     * 파일이 이미지 파일인지 검사
     * - 확장자가 .jpg, .jpeg, .png, .gif 중 하나인지 확인
     *
     * @param filename 검사할 파일명
     * @return 이미지 파일이면 true, 아니면 false
     */
    public static boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif");
    }
}
