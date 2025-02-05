package com.example.mytravellink.api.url;

import com.example.mytravellink.api.url.dto.UrlRequest;
import com.example.mytravellink.api.url.dto.UrlResponse;
import com.example.mytravellink.domain.url.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/url")
@Slf4j
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping // POST 메소드로 설정
    public ResponseEntity<UrlResponse> processUrl(
            @RequestBody UrlRequest request) { // 요청 본문에서 UrlRequest를 가져옴

        // UrlRequest의 URL로 요청 처리
        UrlResponse response = urlService.processUrl(request);
        return ResponseEntity.ok(response);
    }
}