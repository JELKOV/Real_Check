package com.realcheck.util;

import java.util.UUID;

public class FileUtil {

    // 랜덤 파일명 생성 (원본 확장자 유지)
    public static String generateUniqueFileName(String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + ext;
    }

    // 확장자 체크 (필요하면 추가)
    public static boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif");
    }
}
