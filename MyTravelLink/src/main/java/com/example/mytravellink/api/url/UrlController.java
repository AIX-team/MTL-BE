package com.example.mytravellink.api.url;

import com.example.mytravellink.api.url.dto.UrlRequest;
import com.example.mytravellink.api.url.dto.UrlResponse;
import com.example.mytravellink.api.url.dto.UserUrlRequest;
import com.example.mytravellink.domain.url.service.UrlService;
import com.example.mytravellink.auth.handler.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/url")
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final JwtTokenProvider jwtTokenProvider;

    public UrlController(UrlService urlService, JwtTokenProvider jwtTokenProvider) {
        this.urlService = urlService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/analysis")
    public ResponseEntity<UrlResponse> processUrl(
            @RequestBody UrlRequest request) {
        // 여러 URL 중 첫 번째 URL를 기준으로 처리합니다.
        UrlResponse response = urlService.processUrl(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 매핑 API 엔드포인트
     * payload에 포함된 URL들을 기반으로 travel_info_url과 travel_info_place 매핑을 확인/생성하고,
     * 해당 TravelInfo의 id를 리턴합니다.
     */
    @PostMapping("/mapping")
    public ResponseEntity<Map<String, String>> mappingUrl(
            @RequestHeader("Authorization") String token,
            @RequestBody UrlRequest request) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        String travelInfoId = urlService.mappingUrl(request, email);
        Map<String, String> response = new HashMap<>();
        response.put("travelInfoId", travelInfoId);
        return ResponseEntity.ok(response);
    }

    /**
     * 유튜브 자막 체크 API (백엔드에서 FastAPI 자막 체크 엔드포인트 호출)
     */
    @PostMapping("/check_youtube_subtitles")
    public ResponseEntity<Map<String, Object>> checkYoutubeSubtitles(@RequestBody Map<String, String> requestBody) {
        String videoUrl = requestBody.get("video_url");
        boolean hasSubtitles = urlService.checkYoutubeSubtitles(videoUrl);
        Map<String, Object> response = new HashMap<>();
        response.put("video_url", videoUrl);
        response.put("has_subtitles", hasSubtitles);
        return ResponseEntity.ok(response);
    }
}