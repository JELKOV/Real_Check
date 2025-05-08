package com.realcheck.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/naver/search")
public class NaverSearchController {

    @Value("${naver.search.api.url}")
    private String NAVER_API_URL;

    @Value("${naver.search.client.id}")
    private String CLIENT_ID;

    @Value("${naver.search.client.secret}")
    private String CLIENT_SECRET;

    private final RestTemplate restTemplate = new RestTemplate();

    // list.jsp (지도 검색 : 키워드 검색)
    // API 규정상 5개로 제약되어 있음
    @GetMapping
    public ResponseEntity<?> searchNaver(@RequestParam("query") String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", CLIENT_ID);
        headers.set("X-Naver-Client-Secret", CLIENT_SECRET);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = NAVER_API_URL + "?query=" + query + "&display=5&start=1&sort=random";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return ResponseEntity.ok(response.getBody());
    }
}