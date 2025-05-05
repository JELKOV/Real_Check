package com.realcheck.file.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.realcheck.util.FileUtil;

import org.springframework.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * UploadController
 * - 파일 업로드 전용 API 컨트롤러
 * - 클라이언트가 전송한 파일을 서버에 저장하고 URL을 반환함
 */
@RestController // REST API 전용 컨트롤러 (응답은 JSON)
@RequestMapping("/api/upload") // 이 컨트롤러의 기본 URL 경로
public class UploadController {

    // 업로드할 파일을 저장할 기본 디렉토리
    private static final String UPLOAD_DIR = "uploads/";

    /**
     * page: status/my-logs.jsp
     * [1] 파일 업로드 처리
     * - 클라이언트로부터 MultipartFile을 받아 서버에 저장
     * - 저장 경로: uploads/ 폴더
     * - 성공 시 저장된 파일의 URL (/uploads/파일명) 반환
     */
    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }
    
        try {
            // 업로드 디렉토리가 존재하지 않으면 생성
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
    
            // 원본 파일명 확인 및 정리
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return ResponseEntity.badRequest().body("파일명이 없습니다.");
            }
            originalFilename = StringUtils.cleanPath(originalFilename);
    
            // 고유한 파일명 생성 (UUID + 확장자)
            String newFileName = FileUtil.generateUniqueFileName(originalFilename);
    
            // 파일 저장 경로 설정
            Path filePath = Paths.get(UPLOAD_DIR, newFileName);
            Files.copy(file.getInputStream(), filePath);
    
            // 저장된 파일에 대한 접근 URL 생성
            String fileUrl = "/uploads/" + newFileName;
            return ResponseEntity.ok(fileUrl);
    
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 실패");
        }
    }
}
