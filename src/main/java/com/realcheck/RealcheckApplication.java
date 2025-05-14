package com.realcheck;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealcheckApplication {

    public static void main(String[] args) {
        // .env 파일 로드 (루트 디렉토리에 있어야 함)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // .env의 값을 JVM 전역 속성(System properties)으로 등록
        System.setProperty("SPRING_APPLICATION_NAME", dotenv.get("SPRING_APPLICATION_NAME", "realcheck"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        // NAVER 환경 변수 강제 로드
        System.setProperty("NAVER_MAP_CLIENT_ID", dotenv.get("NAVER_MAP_CLIENT_ID"));
        System.setProperty("NAVER_MAP_CLIENT_SECRET", dotenv.get("NAVER_MAP_CLIENT_SECRET"));
        System.setProperty("NAVER_SEARCH_CLIENT_ID", dotenv.get("NAVER_SEARCH_CLIENT_ID"));
        System.setProperty("NAVER_SEARCH_CLIENT_SECRET", dotenv.get("NAVER_SEARCH_CLIENT_SECRET"));
        System.setProperty("NAVER_SEARCH_API_URL", dotenv.get("NAVER_SEARCH_API_URL"));

        // SpringBoot 실행
        SpringApplication.run(RealcheckApplication.class, args);
    }
}
