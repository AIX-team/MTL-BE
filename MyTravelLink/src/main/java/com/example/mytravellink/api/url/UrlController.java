package com.example.mytravellink.api.url;

import com.example.mytravellink.api.url.dto.UrlRequest;
import com.example.mytravellink.api.url.dto.UrlResponse;
import com.example.mytravellink.api.url.dto.UserUrlRequest;
import com.example.mytravellink.domain.url.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/url")
@Slf4j
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/analysis") // POST 메소드로 설정
    public ResponseEntity<UrlResponse> processUrl(
            @RequestBody UrlRequest request) { // 요청 본문에서 UrlRequest를 가져옴

        // UrlRequest의 URL로 요청 처리
        UrlResponse response = urlService.processUrl(request);
        return ResponseEntity.ok(response);
    }

    // 새롭게 추가된 엔드포인트 : 다중 UserUrlRequest를 받아 처리
    @PostMapping("/user/process")
    public ResponseEntity<UrlResponse> processUserUrls(@RequestBody List<UserUrlRequest> requests,
                                                       @RequestHeader("Authorization") String token) {
        // 요청 배열에서 URL 문자열만 추출하여 List<String> 생성
        List<String> urls = requests.stream()
                                    .map(UserUrlRequest::getUrl)
                                    .collect(Collectors.toList());
        // JWT 등에서 이메일 추출 (여기서는 단순 더미 처리)
        String email = extractEmailFromToken(token);
        UrlResponse response = urlService.processUserUrls(urls, email);
        return ResponseEntity.ok(response);
    }
    
    // 간단한 더미 이메일 추출 메서드 (실제 환경에서는 JWT 파싱 활용)
    private String extractEmailFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return "unknown@example.com";
    }
}