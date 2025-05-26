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
import java.util.ArrayList;
import java.util.List;

/**
 * UploadController
 * - 클라이언트에서 전송된 파일을 서버의 로컬 디렉토리에 저장하고
 * - 저장된 파일의 접근 가능한 URL을 JSON 형식으로 응답하는 컨트롤러
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    // 서버에 파일을 저장할 디렉토리 경로
    private static final String UPLOAD_DIR = "uploads/";

    /**
     * [1] 다중 파일 업로드 API (POST /api/upload/multi)
     * page: status/my-logs.jsp
     * page: request/detail.jsp
     * pag: place/register.jsp
     * - 클라이언트에서 여러 파일을 'files' 파라미터로 전송
     * - 각 파일을 서버의 uploads/ 디렉토리에 저장
     * - 저장된 파일의 URL 리스트를 JSON 배열로 반환
     *
     * @param files MultipartFile 배열 (다중 파일 입력)
     * @return 저장된 파일들의 URL 리스트
     */
    @PostMapping("/multi")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> uploadedUrls = new ArrayList<>();

        // 업로드 폴더가 존재하지 않으면 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        for (MultipartFile file : files) {
            try {
                // 비어 있는 파일은 무시
                if (file.isEmpty())
                    continue;

                // 원본 파일명 추출 및 정리
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isBlank())
                    continue;
                String cleaned = StringUtils.cleanPath(originalFilename);

                // 고유 파일명 생성 (UUID + 확장자)
                String newFileName = FileUtil.generateUniqueFileName(cleaned);

                // 파일 저장 경로 설정
                Path filePath = Paths.get(UPLOAD_DIR, newFileName);
                Files.copy(file.getInputStream(), filePath);

                // 접근 가능한 URL 생성 후 리스트에 추가
                uploadedUrls.add("/uploads/" + newFileName);

            } catch (IOException e) {
                e.printStackTrace();
                // 에러 발생 시에도 나머지 파일은 계속 업로드 시도
            }
        }

        return ResponseEntity.ok(uploadedUrls);
    }
}
