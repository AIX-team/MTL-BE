package com.example.mytravellink.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.mytravellink.domain.users.service.UserServiceImpl;
import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.domain.users.entity.UsersSearchTerm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()") 
public class UserController {

    private final UserServiceImpl userService;
    private final JwtTokenProvider jwtTokenProvider;

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
            
            userService.saveSearchTerm(email, request.get("searchTerm"));
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
}
