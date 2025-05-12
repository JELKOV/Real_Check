package com.realcheck.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/reverse-geocode")
public class ReverseGeocodeController {

    @Value("${naver.map.client.id}")
    private String naverMapClientId;

    @Value("${naver.map.client.secret}")
    private String naverMapClientSecret;

    private final String API_URL = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc";

    @GetMapping
    public ResponseEntity<String> getReverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {

        // 디버깅: API 키 값 확인
        System.out.println("Naver Map Client ID: " + naverMapClientId);
        System.out.println("Naver Map Client Secret: " + naverMapClientSecret);

        if (naverMapClientId == null || naverMapClientSecret == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: API Key is not set.");
        }

        String coords = lng + "," + lat;
        String url = API_URL + "?coords=" + coords +
                "&sourcecrs=epsg:4326&orders=roadaddr,addr,admcode,legalcode&output=json";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ncp-apigw-api-key-id", naverMapClientId);
        headers.set("x-ncp-apigw-api-key", naverMapClientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

}
