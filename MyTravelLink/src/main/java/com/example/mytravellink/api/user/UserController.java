package com.example.mytravellink.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.mytravellink.domain.users.service.UserServiceImpl;
import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.domain.users.entity.UsersSearchTerm;
import com.example.mytravellink.api.url.dto.UserUrlRequest;
import com.example.mytravellink.domain.url.service.UrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;

/**
 * 사용자 관련 API를 처리하는 컨트롤러
 * 유튜브 URL 저장, 삭제, 조회 기능을 제공
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()") 
public class UserController {

    private final UserServiceImpl userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UrlService urlService;

    @GetMapping("/travel/info")
    public String travelInfo() {
        return "travel/info";
    }

    // 최근 검색어 조회   
    @GetMapping("/search/recent")
    public ResponseEntity<?> getRecentSearches(@RequestHeader("Authorization") String token) {
        try {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token.replace("Bearer ", ""));
            String email = claims.getSubject();
            
            List<UsersSearchTerm> recentSearches = userService.getRecentSearches(email);
            // 검색어 문자열만 추출하여 반환
            List<String> words = recentSearches.stream()
                .map(UsersSearchTerm::getWord)
                .collect(Collectors.toList());
            return ResponseEntity.ok(words);
        } catch (Exception e) {
            log.error("검색어 조회 실패: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/search/save")
    public ResponseEntity<?> saveSearchTerm(@RequestBody Map<String, String> request,
                                          @RequestHeader("Authorization") String token) {
        try {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token.replace("Bearer ", ""));
            String email = claims.getSubject();
            String searchTerm = request.get("searchTerm");
            
            // 검색어가 비어있는 경우 처리
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("검색어가 비어있습니다.");
            }
            
            // 중복 검사 및 날짜 업데이트를 서비스에서 처리
            userService.updateOrSaveSearchTerm(email, searchTerm.trim());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("검색어 저장 실패: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search/terms")
    public ResponseEntity<?> getSearchTerms(@RequestHeader("Authorization") String token) {
        try {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token.replace("Bearer ", ""));
            String email = claims.getSubject();
            List<UsersSearchTerm> terms = userService.getSearchTerms(email);
            // 검색어 문자열만 추출하여 반환
            List<String> words = terms.stream()
                .map(UsersSearchTerm::getWord)
                .collect(Collectors.toList());
            return ResponseEntity.ok(words);
        } catch (Exception e) {
            log.error("검색어 조회 실패: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveUserUrl(@RequestBody UserUrlRequest request,
                                              @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        try {
            urlService.saveUserUrl(email, request);
            return ResponseEntity.ok("URL이 정상적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("URL 저장 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("URL 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUserUrl(@RequestParam("url") String url,
                                              @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        try {
            // URL 문자열을 전달하여 삭제 처리 (SHA-512 해시 생성 및 삭제)
            urlService.deleteUserUrlByUrl(email, url);
            return ResponseEntity.ok("URL이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("URL 삭제 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("URL 삭제 중 오류 발생: " + e.getMessage());
        }
    }

}
